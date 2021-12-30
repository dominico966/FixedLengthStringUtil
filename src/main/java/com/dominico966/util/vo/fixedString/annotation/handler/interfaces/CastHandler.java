package com.dominico966.util.vo.fixedString.annotation.handler.interfaces;

import java.io.Serializable;

public interface CastHandler<CastFrom, CastTo> {
    CastTo cast(CastFrom data);
}
