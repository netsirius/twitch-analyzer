# Twitch Analyzer
Twitch chat analyzer for personal purposes

## Main modules

* Helix API Handlers: This module contains the relevant methods to handle and make requests to the current Twitch API.
* V5 API Handlers: This second module contains the method to obtain the data referring to the comments of the users in the chat, from the numerical identifier of the video
Transformations: This last module contains the different transformations that are carried out on the extracted data.

## Configurations

The application is configured using environment variables:

* STREAMERS_FILE_PATH: File that contains the list of transmission channel names from which the data will be obtained.
* CHECKPOINT_FILE_PATH: File that mantains the state of last processed video.
* TWITCH_CLIENT_ID: Application level client ID from (https://dev.twitch.tv/console)
* TWITCH_V5_CLIENT_ID: Old V5 client ID.
* TWITCH_CLIENT_SECRET: Secret of the client, obtained from (https://dev.twitch.tv/console)

## Checkpoint Management

The identifier of the last processed video of a streamer is written to the file defined under the CHECKPOINT_FILE_PATH environment variable.

When the videos of a streamer are processed, their status is updated to know the last video processed and only obtain the data of the new videos in the following executions of the application.
