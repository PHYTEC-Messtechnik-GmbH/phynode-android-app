package de.phytec.iot.phynode;

/*
    Copyright 2017  PHYTEC Messtechnik GmbH

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

import java.util.UUID;

public class BleUuid {

    public static class Epaper {
        public final static UUID SERVICE = UUID.fromString("f000aa20-0451-4000-b000-000000000000");
        public final static UUID DATA_INIT = UUID.fromString("f000aa21-0451-4000-b000-000000000000");
        public final static UUID DATA_BUFFER = UUID.fromString("f000aa22-0451-4000-b000-000000000000");
        public final static UUID DATA_UPDATE = UUID.fromString("f000aa23-0451-4000-b000-000000000000");
        public final static UUID CTRL = UUID.fromString("f000aa24-0451-4000-b000-000000000000");
    }

    public static class Color {
        public final static UUID SERVICE = UUID.fromString("f000aa90-0451-4000-b000-000000000000");
        public final static UUID DATA = UUID.fromString("f000aa91-0451-4000-b000-000000000000");
        public final static UUID CTRL = UUID.fromString("f000aa92-0451-4000-b000-000000000000");
    }
}
