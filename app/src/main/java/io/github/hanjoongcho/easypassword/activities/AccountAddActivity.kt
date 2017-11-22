package io.github.hanjoongcho.easypassword.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import io.github.hanjoongcho.easypassword.R
import io.github.hanjoongcho.easypassword.adpaters.AccountCategoryAdapter
import io.github.hanjoongcho.easypassword.databinding.ActivityAccountAddBinding
import io.github.hanjoongcho.easypassword.helper.database
import io.github.hanjoongcho.easypassword.models.Account
import io.github.hanjoongcho.easypassword.models.Category
import io.github.hanjoongcho.utils.PasswordStrengthUtils

/**
 * Created by CHO HANJOONG on 2017-11-18.
 */

class AccountAddActivity : AppCompatActivity() {

    private var mBinding: ActivityAccountAddBinding? = null
    private var mTempStrengthLevel = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_add)

        mBinding = DataBindingUtil.setContentView<ActivityAccountAddBinding>(this,
                R.layout.activity_account_add)

        setSupportActionBar(mBinding?.toolbarPlayer)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
        }

        bindEvent()
        initCategorySpinner()
    }

    private fun bindEvent() {

        mBinding?.let { binding ->

            binding.save.setOnClickListener(View.OnClickListener { _ ->
                val account: Account = Account(
                        binding.accountManageTarget.text.toString(),
                        binding.accountSummary.text.toString(),
                        binding.accountManageCategory.selectedItem as Category,
                        binding.accountId.text.toString(),
                        binding.accountPassword.text.toString(),
                        mTempStrengthLevel
                )
                this@AccountAddActivity.database().insertAccount(account)
                this@AccountAddActivity.onBackPressed()
//                finish()
            })

            binding.accountPassword.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val level = PasswordStrengthUtils.getScore(s.toString())
                    if (level != mTempStrengthLevel) {
                        mTempStrengthLevel = level
                        AccountAddActivity.setPasswordStrengthLevel(this@AccountAddActivity, mTempStrengthLevel, binding.included.level1, binding.included.level2, binding.included.level3, binding.included.level4, binding.included.level5)
                    }
                }
            })
        }
    }

    private fun initCategorySpinner() {
        val adapter: ArrayAdapter<Category> = AccountCategoryAdapter(this@AccountAddActivity, R.layout.item_category, listCategory)
        mBinding?.accountManageCategory?.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> this@AccountAddActivity.onBackPressed()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.account_detail, menu)
//        return true
//    }

    companion object {
        fun getStartIntent(context: Context): Intent = Intent(context, AccountAddActivity::class.java)
        val listCategory: List<Category> = mutableListOf(
                Category(0, "웹사이트", "web"),
                Category(1, "신용카드", "credit_card"),
                Category(2, "도어락", "home"),
                Category(3, "자물쇠", "lock"),
                Category(4, "전자문서", "folder"),
                Category(5, "미분류", "password")
        )
        val listDummyAccount: List<Account> = mutableListOf(
                Account("Google", "https://www.google.com", Category(0, "웹사이트", "web"), "entertainment", "123", 4),
                Account("GitHub", "https://github.com/", Category(0, "웹사이트", "web"), "geography", "1234", 4),
                Account("네이버", "https://www.naver.com/", Category(0, "웹사이트", "web"), "food", "1234", 2),
                Account("카카오뱅크", "카카오뱅크 체크카드", Category(1, "신용카드", "credit_card"), "geography", "1234", 3),
                Account("회사", "회사현관 출입번호", Category(2, "도어락", "home"), "geography", "1234", 1)
        )

        /**
         * Convenience method for color loading.

         * @param colorRes The resource id of the color to load.
         *
         * @return The loaded color.
         */
        fun getColor(@ColorRes colorRes: Int, activity: Activity) = ContextCompat.getColor(activity, colorRes)

        fun setStrengthColor(view: ImageView, colorId: Int) {
            view.setBackgroundColor(colorId)
        }

        fun setPasswordStrengthLevel(activity: Activity, passwordStrengthLevel: Int, level1: ImageView, level2: ImageView, level3: ImageView, level4: ImageView, level5: ImageView) {
            when (passwordStrengthLevel) {
                1 -> {
                    setStrengthColor(level1, getColor(R.color.strength_bad, activity))
                    setStrengthColor(level2, getColor(R.color.strength_default, activity))
                    setStrengthColor(level3, getColor(R.color.strength_default, activity))
                    setStrengthColor(level4, getColor(R.color.strength_default, activity))
                    setStrengthColor(level5, getColor(R.color.strength_default, activity))
                }
                2 -> {
                    setStrengthColor(level1, getColor(R.color.strength_bad, activity))
                    setStrengthColor(level2, getColor(R.color.strength_bad, activity))
                    setStrengthColor(level3, getColor(R.color.strength_default, activity))
                    setStrengthColor(level4, getColor(R.color.strength_default, activity))
                    setStrengthColor(level5, getColor(R.color.strength_default, activity))
                }
                3 -> {
                    setStrengthColor(level1, getColor(R.color.strength_good, activity))
                    setStrengthColor(level2, getColor(R.color.strength_good, activity))
                    setStrengthColor(level3, getColor(R.color.strength_good, activity))
                    setStrengthColor(level4, getColor(R.color.strength_default, activity))
                    setStrengthColor(level5, getColor(R.color.strength_default, activity))
                }
                4 -> {
                    setStrengthColor(level1, getColor(R.color.strength_good, activity))
                    setStrengthColor(level2, getColor(R.color.strength_good, activity))
                    setStrengthColor(level3, getColor(R.color.strength_good, activity))
                    setStrengthColor(level4, getColor(R.color.strength_good, activity))
                    setStrengthColor(level5, getColor(R.color.strength_default, activity))
                }
                5 -> {
                    setStrengthColor(level1, getColor(R.color.strength_good, activity))
                    setStrengthColor(level2, getColor(R.color.strength_good, activity))
                    setStrengthColor(level3, getColor(R.color.strength_good, activity))
                    setStrengthColor(level4, getColor(R.color.strength_good, activity))
                    setStrengthColor(level5, getColor(R.color.strength_good, activity))
                }
            }
        }
    }
}