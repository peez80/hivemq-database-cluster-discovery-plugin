# hivemq database cluster discovery module
Using this module is the easiest way to set up a hivemq cluster. Intentionally it is made to work inside my enterprise grade
hivemq docker image [peez/hivemq](https://hub.docker.com/r/peez/hivemq/) but it will work on all other hivemq installations, too.

## Installation in HiveMQ
If you use the docker image above, no special installation steps are required except preparing a database and passing the suitable environment parameters


* Prepare a database with a user that is allowed to DDL because we use zero configuration and set up our tables on plugin startup.
Easiest way to do this is to spin up a docker container: ```docker run -d -p 5432:5432 postgres:latest```
* Copy the plugin jar to ```hivemq/plugins``` directory 
* Copy config file jdbc-database-cluster-discovery.properties to ```hivemq/conf```
* Edit config file with suitable database access parameters. Unfortunately it's not possible to read ```<reload-interval>``` out of the HiveMQ config - therefore set the value ```inactiveTimeoutMs``` in the properties file to the same value.
* This is the minimalst change for your ```hivemq/conf/config.xml```:

       <cluster>
           <enabled>true</enabled>           
          <discovery>
               <plugin>
                   <reload-interval>20</reload-interval>
               </plugin>
           </discovery>
       </cluster>
    
    Running in a normal environment (not docker) should be sufficient. If you have multiple network interfaces installed probably you will need to specify the listen address. On Docker swarm just use the mentioned docker image above or for sophisticated configuration consult the HiveMQ documentation.


## How it works
For an easy and zero-config set up and update, the plugin uses flyway migrations that are applied on startup. This was we ensure having always a working database table - if an update is needed at some time.
We then just store the known cluster nodes together with their last seen timestamp in a database table.

## Database compatibility
Currently only postgres is supported. Once I find time I will add support for MySql. All other dependencies could be requested or just make a pull request