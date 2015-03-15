
# --- !Ups
alter table chessplayer add grade_estimate varchar(1024);

alter table registration alter column fee type numeric;

# --- !Downs
alter table chessplayer drop grade_estimate;

alter table registration alter column fee type int;