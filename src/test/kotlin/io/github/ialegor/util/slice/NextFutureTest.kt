package io.github.ialegor.util.slice

import org.junit.jupiter.api.DisplayName

@DisplayName("Testing NextFuture, NextRequest and NextResponse")
internal class NextFutureTest : AbstractSliceFutureTest<NextFuture<Int, Int>, NextRequest<Int>, NextResponse<Int, Int>>() {

    override fun createEmptyFuture(): NextFuture<Int, Int> {
        return NextFuture.empty()
    }

    override fun createFutureFromList(items: List<Int>, size: Int): NextFuture<Int, Int> {
        return NextFuture(size) { request ->
            val next = request.next
            val subitems = if (next == null) {
                items.take(size)
            } else {
                items.filter { it > next }.take(size)
            }

            val nnext = subitems.maxOrNull()
            NextResponse(nnext, size, subitems, request)
        }
    }

    override fun NextFuture<Int, Int>.eachSlice(handler: FutureManager.(NextResponse<Int, Int>) -> Unit) {
        return eachNext {
            handler.invoke(this, it)
        }
    }
}