create table nodes (
    clusterId varchar(1000) not null,
    host varchar(1000) not null,
    port int not null,
    lastSeen timestamp not null
);