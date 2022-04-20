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
