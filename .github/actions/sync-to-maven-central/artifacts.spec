{
  "files": [
    {
      "aql": {
        "items.find": {
          "$and": [
            {
              "@build.name": "${buildName}",
              "@build.number": "${buildNumber}",
              "path": {
                "$nmatch": "org/springframework/ws/spring-ws-docs/*"
              }
            }
          ]
        }
      },
      "target": "nexus/"
    }
  ]
}
