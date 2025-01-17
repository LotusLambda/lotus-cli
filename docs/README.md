# Lotus CLI

> The CLI assistant for your Lotus apps.

## What is Lotus CLI?

Lotus CLI is the terminal utility belt for Lotus Lambda apps.
It enables you to create and manage your Lotus apps straight from the comfort of your shell.

### Features
* Create Lotus apps
* Serve Lotus apps in Live mode
* Run Lotus apps in App mode
* Deploy apps to Lotus cloud 
* More coming soon..

## Quick start

### Install
Download the latest release using wget:

```
wget https://bit.ly/3kFvawc && chmod +x lotus-cli && mv lotus-cli /usr/local/bin/
```

or  Curl:

``` 
curl -L https://bit.ly/3kFvawc > lotus-cli && chmod +x lotus-cli && mv lotus-cli /usr/local/bin/

```


### Preview apps

To preview Lotus apps on Android, you can download the Lotus Gallery apk:
[here](https://pjkjhkozufxahmfglihr.supabase.co/storage/v1/object/public/android.debug/app-debug.apk)



### Create

To create a Lotus app inside the current directory, we can invoke


``` 
lotus-cli create

``` 

This will create the basic Lotus file structure and App file.


### Run

After creating the Lotus app, we can use
``` 
lotus-cli run
``` 

inside the app folder to start the Lotus server at localhost:5000.

### Live preview

To enable live preview during the development, we can start the 
preview server by using the `watch` command inside the app folder.

``` 
lotus-cli watch
``` 

This starts a Lotus preview server at localhost:5001, which watches the filesystem for changes and renders the latest updated file as a UI preview.





# Commands


## Create


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


## Run


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



## Watch


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

