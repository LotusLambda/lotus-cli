# Run


### Basics

Run command starts a Lotus app server inside the current app folder.

After using 

``` 
lotus-cli run
``` 

a server starts running the app at localhost:5000 and exposing a QR code to connect from the Lotus Gallery app.

The server allows users to run their Lotus app without having to connect to the Lotus platform for the preview, enabling local and offline development.


### Arguments

Lotus cli supports following arguments:

| Argument | Description                               | Usage                         |
|----------|-------------------------------------------|-------------------------------|
| --src    | Root folder of the Lotus app to run.  | `lotus-cli run --src=MyApp` |
| --port   | Port to start the Lotus app server on | `lotus-cli run --port=5000` |



