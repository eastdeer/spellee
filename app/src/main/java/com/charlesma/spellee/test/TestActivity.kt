package com.charlesma.spellee.test

import com.charlesma.spellee.R
import com.charlesma.spellee.base.BaseActivity

class TestActivity : BaseActivity() {
    override fun onTtsInitSuccess() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTtsInitFailed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContentLayoutResId(): Int {
        return R.layout.content_test
    }
}
