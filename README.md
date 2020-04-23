# Spring AWS DynamoDB Cache

db-cache implements the Spring Framework Context Caching interfaces 
and can be used alongside with the `@Cacheable` annotation.

See `cloud.angst.dbcache.app.config.DBCacheConfig` how to set it up.


## Why?!

So why would you use the rather slow AWS DyanmoDB service than - let's say - Redis?

It depends on what your requirements are.
- Maybe you cannot foresee the amount of data you will have and want to start slow.
- The data you are storing must be searchable afterwards.
- You want to be able to modify the data using the UI provided for the DynamoDB by AWS. 
- One constraint might be related to costs. If you have many items to cache and need to
keep them a long time, Redis will cost you a fortune (600+ $).

## Trade-Offs

As always, there are trade-offs. The biggest one being time. Redis and other cache solutions
are lighting fast (< 1ms). DynamoDB speed will vary based on the data already stored (1-10ms).

There is no automatic cleanup for entries which exceeded their time to live.  

## Cache Table Structure

### cache-key

The table's partition key to access the value from is serialized using `String.valueOf`. 
Please use `SPEL` if your key is more complex to provide a string key.


### cache-ttl
If you provided a configuration to expire entries after a fixed duration the time 
it will expire will be stored in the column `cache-ttl` in ISO-8603 format.

Please be aware that entries are only evicted if they are queried.

### cache-type

Based on what you are caching, the type can be `simple` or `complex`.
Simple types will be store in one column (`cache-value`). Complex types like objects or maps
will be stored in multiple columns where the column name will be the class variable name.

## Example Configuration

How you use and configure the caching is up to you :)

    spring:
      cache:
        dbcache:
          manager:
            table-prefix: "cache-"
            auto-create-cache-tables: true
          cache:
            simple:
              value-class: java.lang.String
              ttl: 30m
            graph:
              value-class: cloud.angst.dbcache.app.model.Graph
              ttl: 0
            list:
              value-class: java.util.ArrayList
              ttl: 2

