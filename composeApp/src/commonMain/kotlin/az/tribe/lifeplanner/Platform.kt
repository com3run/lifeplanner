package az.tribe.lifeplanner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform