/*
Copyright 2013 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.twitter.chill;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

import static com.esotericsoftware.kryo.util.Util.className;

/**
 * Use reflection to instantiate a serializer.
 * Used when serializer classes are written to config files
 */
public class ReflectingRegistrar<T> implements IKryoRegistrar {
    final Class<T> klass;
    // Some serializers handle any class (FieldsSerializer, for instance)
    final Class<? extends Serializer<?>> serializerKlass;

    public Class<T> getRegisteredClass() {
        return klass;
    }

    public Class<? extends Serializer<?>> getSerializerClass() {
        return serializerKlass;
    }

    public ReflectingRegistrar(Class<T> cls, Class<? extends Serializer<?>> ser) {
        klass = cls;
        serializerKlass = ser;
    }

    @Override
    public void apply(Kryo k) {
        k.register(klass, newSerializer(serializerKlass, klass));
    }

    protected Serializer newSerializer(Class<? extends Serializer> serializerClass, Class type) {
        try {
            try {
                return serializerClass.getConstructor(Kryo.class, Class.class).newInstance(this, type);
            } catch (NoSuchMethodException ex1) {
                try {
                    return serializerClass.getConstructor(Kryo.class).newInstance(this);
                } catch (NoSuchMethodException ex2) {
                    try {
                        return serializerClass.getConstructor(Class.class).newInstance(type);
                    } catch (NoSuchMethodException ex3) {
                        return serializerClass.newInstance();
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to create serializer \"" + serializerClass.getName() + "\" for class: "
                    + className(type), ex);
        }
    }

    @Override
    public int hashCode() {
        return klass.hashCode() ^ serializerKlass.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (null == that) {
            return false;
        } else if (that instanceof ReflectingRegistrar) {
            return klass.equals(((ReflectingRegistrar) that).klass) &&
                    serializerKlass.equals(((ReflectingRegistrar) that).serializerKlass);
        } else {
            return false;
        }
    }
}
