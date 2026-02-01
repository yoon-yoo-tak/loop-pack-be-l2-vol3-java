package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;

public class BrandAdminV1Dto {

    public record CreateRequest(String name) {
    }

    public record UpdateRequest(String name) {
    }

    public record BrandResponse(Long id, String name) {
        public static BrandResponse from(BrandInfo info) {
            return new BrandResponse(info.id(), info.name());
        }
    }
}
