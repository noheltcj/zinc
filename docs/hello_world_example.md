# Examples
Use the following data class as a starting point for each classification.

```kotlin
data class Hello(
    val id: String,
    val label: String,
    val world: World
)
```

## Generated Production Builder
Below is the builder and DSL that's generated from the above data class.

```kotlin
import com.noheltcj.example.model.World
import com.noheltcj.zinc.core.BuilderProperty
import com.noheltcj.zinc.core.ZincBuilder
import kotlin.String

class HelloBuilder : ZincBuilder<Hello> {
    private var _id: String by BuilderProperty<String>(
        propertyDescription = idDescription
    )

    private var _label: String by BuilderProperty<String>(
        propertyDescription = labelDescription
    )

    private var _world: World by BuilderProperty<World>(
        propertyDescription = worldDescription
    )

    fun id(value: String): HelloBuilder {
        this._id = value
        return this
    }

    fun label(value: String): HelloBuilder {
        this._label = value
        return this
    }

    fun world(value: World): HelloBuilder {
        this._world = value
        return this
    }

    override fun build() = Hello(
        id = this._id,
        label = this._label,
        world = this._world
    )

    companion object {
        private const val builderName = "HelloBuilder"
        private const val idDescription =
            "$builderName property \"id\" with type: String and no default value"
        private const val labelDescription =
            "$builderName property \"label\" with type: String and no default value"
        private const val worldDescription =
            "$builderName property \"world\" with type: World and no default value"

        @JvmStatic inline fun buildHello(crossinline block: HelloBuilder.() -> Unit): Hello =
            HelloBuilder().apply(block).build()
    }
}
```

## Generated Test Builder
_To be implemented soon._
