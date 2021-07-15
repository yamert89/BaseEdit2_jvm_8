import roslesinforg.porokhin.areaviews.StrictAreaController
import roslesinforg.porokhin.areaviews.StrictAreaView

class BE2StrictView: StrictAreaView() {
    override val controller = find(GenController::class)
    init {
        initialize()
    }

    override fun initialize() {
        controller.setStrictView(this)
        root.construct()
    }
}