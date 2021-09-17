package com.noheltcj.example.test

import com.google.common.truth.Truth.assertThat
import com.noheltcj.example.Main.randomStringProperty
import com.noheltcj.example.model.HelloBuilder.Companion.buildHello
import com.noheltcj.example.model.WorldBuilder.Companion.buildWorld
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

object ZincEndToEndTest : Spek({
    val fakeLabel by memoized(mode = CachingMode.TEST) { randomStringProperty }

    val helloByTransitiveProductionBuilder by memoized(mode = CachingMode.TEST) {
        buildHello {
            id(randomStringProperty)
            label(fakeLabel)
            world(buildWorld())
        }
    }

    describe("production builder transitively inherited from dependent configuration") {
        it("should produce an effective builder") {
            assertThat(helloByTransitiveProductionBuilder.label).isEqualTo(fakeLabel)
        }
    }
})
