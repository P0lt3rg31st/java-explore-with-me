--liquibase formatted sql

--changeset petrichor:004-distance-fn splitStatements:false endDelimiter:$$
CREATE
OR
REPLACE
FUNCTION distance(lat1 double precision, lon1 double precision,
                                    lat2 double precision, lon2 double precision)
RETURNS double precision
AS $$
DECLARE
    dist double precision;
    rad_lat1 double precision;
    rad_lat2 double precision;
    rad_theta double precision;
BEGIN
    IF lat1 = lat2 AND lon1 = lon2 THEN
        RETURN 0;
    END IF;

    rad_lat1 := pi() * lat1 / 180;
    rad_lat2 := pi() * lat2 / 180;
    rad_theta := pi() * (lon1 - lon2) / 180;

    -- spherical law of cosines
    dist := sin(rad_lat1) * sin(rad_lat2)
          + cos(rad_lat1) * cos(rad_lat2) * cos(rad_theta);

    IF dist > 1 THEN
        dist := 1;
    ELSIF dist < -1 THEN
        dist := -1;
    END IF;

    -- earth radius (km) * central angle
    RETURN 6371.0 * acos(dist);
END;
$$ LANGUAGE plpgsql IMMUTABLE STRICT;
$$