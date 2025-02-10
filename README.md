# Basic Exoplayer app

This is a bootstrap of Exoplayer powered application, only able to load an url and play it for now.

The objective is to implement a basic play queue management on your own. Do not use ExoPlayer playlist API. App should allow to:

Display the current media queue, alongside the player view
Chain playback from a media to the next one in the list
Start playback of any media in the queue on click on this media
Add or remove a media from this queue
Media can be local media (on device), embedded in assets or urls to stream from, your choice!

The goal of this exercise is to implement the player and queue list modules how you prefer, with the framework you want (Androidx Viewmodel is used to bootstrap the app here, keep it or replace it, it doesn’t matter for this exercice). You can even start from scratch if you prefer. UI appearance and UI robustness are not important, we are focusing on the under layers and how they are exposed and used.

We are not expecting any particular architectural pattern or framework to use. This will be a basis for the next interview meeting.

## Getting Started

The project is runnable using the latest Android Studio Ladybug and gradle version 8.12.1

### Installing

Simply run the app configuration on a device or emulator (version >= 23) 

## Testing

Testing hasn't been done thoroughly. The goal here is to provide a sample of what unit testing can look like
and that the code is easily testable.

### Running the test
Either right click on the test package or run the `testDebugUnitTest` gradle task.

### Tests structure

Unit test methods are using the standard `Given... When... Then` structure to describe the logic being tested.
Tests are written in a flat way: no usage of inner classes and the *@Nested* and *@DisplayName* annotations, but that could be easily done since JUnit 5 is already configured.

### Mocking
While mocking has been used to get around the usage of some classes from the android framework in the ViewModel, I chose not to mock every dependencies as this would only bring more overhead.

## Built With

- MVVM architecture and the unidirectional data flow
- Clean architecture (as much as possible without creating too much boilerplate code for a simple home assignment)
- Coroutine Flows
- Coil for image loading
- Ktor for handling network requests and response 
- Jetpack Compose for the UI

## Remarks

If this would be the base for a real project, here are some of the aspects of the project I would improve (from more to less important)
- Complete unit testing of existing code base (focusing on ViewModels and Use Cases classes)
- Background support: to ensure the app continues to work after being sent to the background, we would need to create a foreground service
- Provide control over the playback through the notification center
- Media caching (for tracks and images) 
- UI, navigation and configuration changes handling

## Reviewing

Since this is available on github, feel free to add your comments and questions in a PR as base for our discussion.
