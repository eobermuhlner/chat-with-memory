# AI Chat Application

## Overview

The AI Chat Application is a robust platform designed to have interactive and personalized conversations with multiple AI assistants. Each assistant possesses a unique personality, allowing for diverse and engaging interactions. Users can create and manage multiple chat sessions, each dedicated to specific topics.

Additionally, the application supports document access for assistants and chats, enhancing the quality of responses.

Assistants also have long-term memory capabilities, ensuring continuity and context retention across sessions.

## Features

### Multiple Assistants with Specific Personalities
- **Diverse Interactions:** Each assistant is designed with a distinct personality to cater to different user preferences and interaction styles.
- **Customizable:** Users can select from a variety of assistants based on their conversational needs and topics of interest.

### Multiple Chats with Specific Topics
- **Topic-Based Chats:** Users can create and manage multiple chat sessions, each focused on a specific topic or area of interest.
- **Organized Conversations:** Keep discussions organized and relevant by dedicating separate chats for different subjects.

### Document Access for Assistants and Chats
- **Enhanced Responses:** Assistants and chat sessions can access specific documents to provide informed and contextually relevant answers.
- **Document Management:** Users can upload and assign documents to specific assistants and chats, ensuring that relevant information is readily available.

### Long-Term Memory for Assistants
- **Context Retention:** Assistants remember previous interactions and user preferences, allowing for seamless continuity across multiple sessions.
- **Personalized Experience:** Long-term memory enables assistants to provide more personalized and context-aware responses over time.

### Real-Time Information Retrieval
- **Up-to-Date Information:** Tools are available that can fetch up-to-date information, such as news, weather, public transport schedules in Switzerland, and general web searches.
- **Tool Integration:** These tools can be assigned to specific chats and assistants to enhance the relevance and accuracy of the information provided.

### Memory Management through Progressive Summarization
- **Short-Term Memory:** Keeps the last few messages in the context of the next prompt, ensuring immediate continuity in the conversation.
- **Level 1 Long-Term Memory:** When enough messages accumulate in short-term memory, they get summarized and moved to level 1 long-term memory.
- **Level 2 Long-Term Memory:** As more level 1 summaries accumulate, they are further summarized into level 2 long-term memory.
- **Retrieval-Augmented Generation (RAG):** Additionally, relevant older messages are retrieved via RAG to ensure contextually accurate and relevant responses even from historical conversations.

## Getting Started

### Usage
1. **Create a New Chat:** Start by creating a new chat session and assigning a specific topic.
2. **Select an Assistant:** Choose an assistant that best matches your desired interaction style.
3. **Upload Documents:** If necessary, upload documents that you want the assistant to access during the chat.
4. **Start Chatting:** Begin your conversation with the assistant. The assistant will remember previous interactions and use the documents provided to enhance responses.
5. **Manage Chats and Assistants:** Use the application interface to manage your chat sessions and switch between different assistants as needed.

## Contributing
We welcome contributions to enhance the AI Chat Application. If you have suggestions or would like to contribute to the development, please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Commit your changes and push to your branch.
4. Submit a pull request with a detailed description of your changes.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

---

# Examples

## Chat Examples

### Software Development
```
Software development:
- Kotlin (default)
- Java
- Spring
- JPA
- Kafka
- PostgreSQL
- H2

If you have no relevant answer or the answer was already given, respond with NO_ANSWER
```

## Assistant Examples

### Software Code Reviewer

```
You are Ada, a professional code reviewer.
Your role is to do code reviews and provide constructive criticism.
Your responses are always concise and to the point.
You respond only under these conditions:
- When somebody asks explicitly for a code review
- When you are addressed directly
- When you see code written by other assistants that has issues
In all other cases you will not respond.
```
