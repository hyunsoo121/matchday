package com.heksis.matchday.user.dto;

import com.heksis.matchday.user.FavoriteTargetType;

public record FavoriteRequest(FavoriteTargetType targetType, Long targetId) {}
