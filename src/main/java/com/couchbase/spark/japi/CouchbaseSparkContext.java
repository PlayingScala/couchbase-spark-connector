/*
 * Copyright (c) 2015 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.spark.japi;

import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.view.SpatialViewQuery;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.spark.RDDFunctions;
import com.couchbase.spark.connection.SubdocLookupResult;
import com.couchbase.spark.connection.SubdocLookupSpec;
import com.couchbase.spark.rdd.*;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CouchbaseSparkContext {

    private final SparkContext sc;

    protected CouchbaseSparkContext(SparkContext sc) {
        this.sc = sc;
    }

    public static CouchbaseSparkContext couchbaseContext(SparkContext sc) {
        return new CouchbaseSparkContext(sc);
    }

    public static CouchbaseSparkContext couchbaseContext(JavaSparkContext sc) {
        return new CouchbaseSparkContext(sc.sc());
    }

    public JavaRDD<JsonDocument> couchbaseGet(List<String> ids, String bucket) {
        return couchbaseGet(ids, bucket, JsonDocument.class);
    }

    public JavaRDD<JsonDocument> couchbaseGet(List<String> ids) {
        return couchbaseGet(ids, null, JsonDocument.class);
    }

    public <D extends Document> JavaRDD<D> couchbaseGet(List<String> ids, Class<D> clazz) {
        return couchbaseGet(ids, null, clazz);
    }

    @SuppressWarnings({"unchecked"})
    public <D extends Document> JavaRDD<D> couchbaseGet(List<String> ids, String bucket, Class<D> clazz) {
        return new KeyValueRDD(
            sc,
            SparkUtil.listToSeq(ids),
            bucket,
            SparkUtil.classTag(clazz)
        ).toJavaRDD();
    }

    public JavaRDD<SubdocLookupResult> couchbaseSubdocLookup(List<String> ids, List<String> get) {
        return couchbaseSubdocLookup(ids, get, Collections.<String>emptyList(), null);
    }

    public JavaRDD<SubdocLookupResult> couchbaseSubdocLookup(List<String> ids, List<String> get, List<String> exists) {
        return couchbaseSubdocLookup(ids, get, exists, null);
    }

    @SuppressWarnings({"unchecked"})
    public JavaRDD<SubdocLookupResult> couchbaseSubdocLookup(List<String> ids, List<String> get, List<String> exists,
        String bucket) {
        List<SubdocLookupSpec> specs = new ArrayList<SubdocLookupSpec>(ids.size());
        for (String id : ids) {
            specs.add(new SubdocLookupSpec(id, SparkUtil.listToSeq(get), SparkUtil.listToSeq(exists)));
        }
        return new SubdocLookupRDD(
            sc,
            SparkUtil.listToSeq(specs),
            bucket
        ).toJavaRDD();
    }

    public JavaRDD<CouchbaseViewRow> couchbaseView(final ViewQuery query) {
        return couchbaseView(query, null);
    }

    public JavaRDD<CouchbaseViewRow> couchbaseView(final ViewQuery query, final String bucket) {
        return new ViewRDD(sc, query, bucket, null).toJavaRDD();
    }

    public JavaRDD<CouchbaseSpatialViewRow> couchbaseSpatialView(final SpatialViewQuery query) {
        return couchbaseSpatialView(query, null);
    }

    public JavaRDD<CouchbaseSpatialViewRow> couchbaseSpatialView(final SpatialViewQuery query, final String bucket) {
        return new SpatialViewRDD(sc, query, bucket).toJavaRDD();
    }

    public JavaRDD<CouchbaseQueryRow> couchbaseQuery(final N1qlQuery query) {
        return couchbaseQuery(query, null);
    }

    public JavaRDD<CouchbaseQueryRow> couchbaseQuery(final N1qlQuery query, final String bucket) {
        return new QueryRDD(sc, query, bucket).toJavaRDD();
    }

}
