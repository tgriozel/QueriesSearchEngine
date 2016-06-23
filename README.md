# What is this?

This is a little query search engine simulation, based on date ranges.  
The program reads a tsv file containing all the dated queries (its path is specified in the
[config](src/main/resources/application.conf)).  
Each line has to be a timestamp (format: `yyyy-MM-dd HH:mm:ss`) followed by a tab and then a query.

A REST API is made available to get those queries based on date ranges and (optionally) the N most submitted queries.

# Examples

 * Distinct queries done in 2015: `GET /1/queries/count/2015`: returns `{ count: 573697 }`
 * Distinct queries done on Aug 3rd: `GET /1/queries/count/2015-08-03`: returns `{ count: 198117 }`
 * Distinct queries done on Aug 1st between 00:04:00 and 00:04:59: `GET /1/queries/count/2015-08-01 00:04`: returns `{ count: 617 }`

 * Top 3 popular queries done in 2015: `GET /1/queries/popular/2015?size=3`: returns

```js
    {
      queries: [
        { query: "http%3A%2F%2Fwww.getsidekick.com%2Fblog%2Fbody-language-advice", count: 6675 },
        { query: "http%3A%2F%2Fwebboard.yenta4.com%2Ftopic%2F568045", count: 4652 },
        { query: "http%3A%2F%2Fwebboard.yenta4.com%2Ftopic%2F379035%3Fsort%3D1", count: 3100 }
      ]
    }
```

# Building and running

*Please note that RestApi and RestApiSpec will use a concrete value store and try to load the data file specified in the project
configuration.*

You will just need sbt for that:  
`sbt assembly`  
`sbt run`  

To run the tests:  
`sbt test`
