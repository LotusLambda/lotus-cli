# Basics of using Lotus-cli


# Create


### Basics

Create command simply creates a basic Lotus app inside the current app folder.

After using 

``` 
lotus-cli create
``` 

the user is given a name prompt, which can also be passed in by using the `--name`
flag:

``` 
lotus-cli create --name=MyApp
``` 

### Created structure

The created file/folder structure is currently as follows:

* AppFolder 
	* /app/
		* App.lts
	* /actions/
		* actions.js 


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



# Watch


### Basics

Watch command starts a Lotus preview server inside the current app folder.

After using 

``` 
lotus-cli watch
``` 

a server starts running the app at localhost:5001 and exposing a QR code to connect from the Lotus Gallery app.

The preview server allows users to see the changes they make to Lotus files live on their phone, making development faster than ever.


### Arguments

Lotus cli supports following arguments:

| Argument | Description                               | Usage                         |
|----------|-------------------------------------------|-------------------------------|
| --src    | Root folder of the Lotus app to preview.  | `lotus-cli watch --src=MyApp` |
| --port   | Port to start the Lotus preview server on | `lotus-cli watch --port=5001` |
| --web | Turn on the web preview. | `lotus-cli watch --web` |

*Web argument is coming in the next release*


### How it works

The watch command starts watching for filesystem changes inside the root folder (recursively). After a  .lts file is updated, the server dispatches the updated version of the component to the connected preview apps.

