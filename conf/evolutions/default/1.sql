# --- First database schema

# --- !Ups

create table chessplayer (
  id                        serial not null primary key,
  first_name                varchar(127) not null,
  last_name                 varchar(127) not null,	
  email                     varchar(255) not null,	
  address	                  varchar(1024) not null,	
  postcode                  varchar(8) not null,
  phone                     varchar(32) not null,
  date_of_birth             date,
  chessclub                 varchar(255),
  ecf_no                    varchar(16),
  ecf_grade_ref             varchar(16),
  rapid_grade               int,
  slow_grade                int
);

create table registration (
  id                        serial not null primary key,
  section                   varchar(32) not null,
  fee                       int not null,
  payment_confirmed         boolean not null,
  bye_rounds                varchar(127),
  player_id                 bigint not null references chessplayer(id)
);

create table pp_notification (
  id                        serial not null primary key,
  registration_id           int,
  receiver_email            varchar(255),
  payment_amount            varchar(16),
  payment_ccy               varchar(8),
  payment_status            varchar(32),
  verification_status       varchar(32),
  first_name                varchar(127),
  last_name                 varchar(127),
  full_notification         varchar(4096)
);
 
create index ix_registration_chessplayer_1 on registration (player_id);
create index ix_pp_notification_1 on pp_notification (registration_id);

# --- !Downs


drop table if exists pp_notification;
drop table if exists registration;
drop table if exists chessplayer;

