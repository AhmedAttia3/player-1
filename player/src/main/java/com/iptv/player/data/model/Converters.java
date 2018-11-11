package com.iptv.player.data.model;

import io.objectbox.converter.PropertyConverter;

public class Converters {

    public static class TypeConverter implements PropertyConverter<Type, Integer> {

        @Override
        public Type convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }
            for (Type type : Type.values()) {
                if (type.id == databaseValue) {
                    return type;
                }
            }
            return Type.LIVE;
        }

        @Override
        public Integer convertToDatabaseValue(Type entityProperty) {
            return entityProperty == null ? null : entityProperty.id;
        }
    }
}
