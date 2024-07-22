# General notes about the `chat-with-memory` project

- document store (RAG) for:
  - user documents 
  - assistant documents
  - chat documents

- use token count instead of message count to decide: 
  - when to summarize
  - how many relevant messages retrieved using RAG 

- use a real embeddings vector database (especially for the persistence)


## Additional libraries that could help

- https://github.com/knuddelsgmbh/jtokkit
  Java implementation to tokenize (and count tokens) text