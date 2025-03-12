/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
 *
 * ArchitectFX is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ArchitectFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArchitectFX. If not, see <http://www.gnu.org/licenses/>.
 */

package misc;

import io.github.palexdev.architectfx.backend.loaders.UILoader;
import io.github.palexdev.architectfx.backend.resolver.DefaultResolver;
import io.github.palexdev.architectfx.backend.resolver.Resolver;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class NonStaticInjectTestClass {

    public UILoader.Loaded<NonStaticInjectTestClass> load() throws IOException {
        String doc = """
            .controller: misc.NonStaticInjectTestClass$Controller {
                injected: $aString$
            }
            
            NonStaticInjectTestClass {
              .cid: 'obj'
            }
            """;
        DummyLoader<NonStaticInjectTestClass> loader = new DummyLoader<>();
        loader.config().setResolverFactory(l -> {
            Resolver.Context ctx = new Resolver.Context(l);
            ctx.setInjections("aString", "Successfully Injected!");
            return new DefaultResolver(ctx);
        });
        loader.config().setControllerFactory(NonStaticInjectTestClass.Controller::new);
        return loader.load(new ByteArrayInputStream(doc.getBytes()), null);
    }

    public class Controller {
        public String aString;
    }
}
