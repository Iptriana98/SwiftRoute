package com.swiftroute.app.domain.model

data class Route(
    val id: String,
    val name: String,
    val stops: List<Stop>,
    val status: RouteStatus = RouteStatus.Draft,
    val optimizationCriterion: OptimizationCriterion = OptimizationCriterion.Fastest
)
