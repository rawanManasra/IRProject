
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required byOCP applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class HandsOnDemo {
	private static final String BODY_FIELD = "body";
	private static final FieldType TERM_VECTOR_TYPE;
	static {
		TERM_VECTOR_TYPE = new FieldType(TextField.TYPE_STORED);
		TERM_VECTOR_TYPE.setStoreTermVectors(true);
		TERM_VECTOR_TYPE.setStoreTermVectorPositions(true);
		TERM_VECTOR_TYPE.setStoreTermVectorOffsets(true);
		TERM_VECTOR_TYPE.freeze();
	}

	private static final String[][] DATA = new String[][] {
			new String[] { "doc0", "First document with one sentence." },
			new String[] { "doc1", "Second document. With two sentences." } };

	public static void main(String[] args) throws Exception {
		try (Directory dir = newDirectory(); Analyzer analyzer = newAnalyzer()) {
			// Index
			try (IndexWriter writer = new IndexWriter(dir, newIndexWriterConfig(analyzer))) {
				for (final String[] docData : DATA) {
					final Document doc = new Document();
					doc.add(new StringField("id", docData[0], Store.YES));
					doc.add(new TextField("body", docData[1], Store.YES));
					// doc.add(new Field(BODY_FIELD, docData[1], TERM_VECTOR_TYPE));
					writer.addDocument(doc);
				}
			}

			// Search
			try (DirectoryReader reader = DirectoryReader.open(dir)) {
				logIndexInfo(reader);

				final QueryParser qp = new QueryParser(BODY_FIELD, analyzer);
				final Query q = qp.parse("sentences");
				System.out.println("Query: " + q);
				System.out.println();

				final IndexSearcher searcher = new IndexSearcher(reader);
				final TopDocs td = searcher.search(q, 10);

				final FastVectorHighlighter highlighter = new FastVectorHighlighter();
				final FieldQuery fieldQuery = highlighter.getFieldQuery(q, reader);
				for (final ScoreDoc sd : td.scoreDocs) {
					final String[] snippets = highlighter.getBestFragments(fieldQuery, reader, sd.doc, BODY_FIELD, 100,
							3);
					final Document doc = searcher.doc(sd.doc);
					System.out.println(Utils.format("doc=%d, score=%.4f, text=%s snippet=%s", sd.doc, sd.score,
							doc.get(BODY_FIELD), Arrays.stream(snippets).collect(Collectors.joining(" "))));
				}
			}
		}

	}

	private static Directory newDirectory() throws IOException {
		// return new RAMDirectory();
		return FSDirectory.open(new File("d:/tmp/ir-class/demo").toPath());
	}

	private static Analyzer newAnalyzer() {
		// return new WhitespaceAnalyzer();
		// return new StandardAnalyzer();
		return new EnglishAnalyzer();
	}

	private static IndexWriterConfig newIndexWriterConfig(Analyzer analyzer) {
		return new IndexWriterConfig(analyzer).setOpenMode(OpenMode.CREATE).setCodec(new SimpleTextCodec())
				// .setUseCompoundFile(false)
				.setCommitOnClose(true);
	}

	private static void logIndexInfo(IndexReader reader) throws IOException {
		System.out.println("Index info:");
		System.out.println("----------");
		System.out.println("Docs: " + reader.numDocs());
		final Fields fields = MultiFields.getFields(reader);
		// instead of iterator was iterable.
		System.out.println("Fields: " + fields.iterator().toString());
		System.out.println("Terms:");
		for (final String field : fields) {
			final Terms terms = MultiFields.getTerms(reader, field);
			System.out.println(Utils.format("  %s (sumTTF=%d sumDF=%d)", field, terms.getSumTotalTermFreq(),
					terms.getSumDocFreq()));
			final TermsEnum termsEnum = terms.iterator();
			final StringBuilder sb = new StringBuilder();
			while (termsEnum.next() != null) {
				sb.append("    ").append(termsEnum.term().utf8ToString());
				sb.append(" (").append(termsEnum.docFreq()).append(")");
				sb.append("  docs=");
				final PostingsEnum postings = termsEnum.postings(null, PostingsEnum.ALL);
				while (postings.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
					sb.append("{id=").append(postings.docID()).append(',');
					sb.append("freq=").append(postings.freq());
					sb.append(",pos=[");
					for (int i = 0; i < postings.freq(); i++) {
						final int pos = postings.nextPosition();
						sb.append(pos).append(',');
					}
					sb.append(']');
					sb.append("} ");
				}
				System.out.println(sb);
				sb.setLength(0);
			}
		}
		System.out.println();
	}

}
