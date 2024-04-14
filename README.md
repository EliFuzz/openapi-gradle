# OpenAPI Gradle Wrapper

## Usage

1. In `build.gradle.kts` file replace the path to OpenAPI specification file `contractInputSpec`
2. Publish to maven repo

```shell
./gradlew publishToMavenLocal
```

3. The packages will be created
    - client - group: `com.genten.contract` artifactId: `client`
    - server - group: `com.genten.contract` artifactId: `server` 
