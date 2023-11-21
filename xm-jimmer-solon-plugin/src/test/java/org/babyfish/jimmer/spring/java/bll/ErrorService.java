package org.babyfish.jimmer.spring.java.bll;

import org.babyfish.jimmer.client.ThrowsAll;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;

import java.math.BigDecimal;

@Controller
public class ErrorService {

    @Get
    @Mapping("/error/test")
    @ThrowsAll(GeographyErrorCode.class)
    public void test() {
        try {
            validatePosition();
        } catch (NestedPosException ex) {
            throw GeographyException.illegalPosition(
                    "Illegal geography position which is already marked",
                    ex,
                    ex.longitude,
                    ex.latitude
            );
        }
    }

    private void validatePosition() {
        throw new NestedPosException(
                new BigDecimal("104.06"),
                new BigDecimal("30.67")
        );
    }

    private static class NestedPosException extends RuntimeException {

        final BigDecimal longitude;

        final BigDecimal latitude;

        NestedPosException(BigDecimal longitude, BigDecimal latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }
}
