# e-commerce app

Rest-APIs that simulates an e-commerce processes

## Tests

To run Unit Tests:

```
sbt test
```

To run Integration Tests we need to run both `PostgreSQL` and `Redis`:

```
docker-compose up
sbt it:test (Inside the root folder)
docker-compose down
```

## Build Docker image

```
sbt docker:publishLocal
```

To run our application using our Docker image, run the following command:

```
cd /app
docker-compose up
```
