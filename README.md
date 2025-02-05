# Basic Exoplayer app

This is a bootstrap of Exoplayer powered application, only able to load an url and play it for now.

The objective is to implement a basic play queue management on your own. Do not use ExoPlayer playlist API.
App should allow to:

- Display the current media queue, alongside the player view
- Chain playback from a media to the next one in the list
- Start playback of any media in the queue on click on this media
- Add or remove a media from this queue

Media can be local media (on device), embedded in assets or urls to stream from, your choice!

The goal of this exercise is to implement the player and queue list modules how you prefer, with the framework you want (Androidx Viewmodel is used to bootstrap the app here, keep it or replace it, it doesn’t matter for this exercice).
You can even start from scratch if you prefer.
UI appearance and UI robustness are not important, we are focusing on the under layers and how they are exposed and used.


We are not expecting any particular architectural pattern or framework to use. This will be a basis for the next interview meeting.
