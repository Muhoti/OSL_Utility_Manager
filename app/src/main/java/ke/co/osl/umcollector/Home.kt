package ke.co.osl.umcollector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import ke.co.osl.umcollector.Adapter.RecylerAdapter
import ke.co.osl.umcollector.api.ApiInterface
import ke.co.osl.umcollector.models.ChangePasswordBody
import ke.co.osl.umcollector.models.Message

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Home : AppCompatActivity() {

    lateinit var dialog: Dialog
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarToggle: ActionBarDrawerToggle
    lateinit var navView: NavigationView
    lateinit var resetPasswordDialog: Dialog
    lateinit var userDetailsDialog: Dialog
    lateinit var webView: WebView
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var accuracy: TextView
    lateinit var tally: TextView
    lateinit var coords: TextView
    lateinit var options: ImageView
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    var lat: Double = 0.0
    var lng: Double = 0.0
    var acc: Float = 0f
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //This section comprises of the navigation tools
        toolbar = findViewById(R.id.appbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer)
        navView = findViewById(R.id.nav_view)

        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.isDrawerIndicatorEnabled
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        actionBarToggle.syncState()
        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> goHome()
                R.id.mappedPoints -> mappedPoints()
                R.id.waterPipes -> mappedWaterPipes()
                R.id.sewerlines -> mappedSewerLines()
                R.id.changePassword -> changePassword()
                R.id.userDetails -> showUserInfo()
                R.id.logout -> signOut()
            }

            true
        }

        resetPasswordDialog = Dialog(this)
        resetPasswordDialog.setContentView(R.layout.reset_password)

        userDetailsDialog = Dialog(this)
        userDetailsDialog.setContentView(R.layout.user_details_dialogue)

        preferences = this.getSharedPreferences("ut_manager", MODE_PRIVATE)
        editor = preferences.edit()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        init()

    }

    private fun init() {
        var waterRecycler: RecyclerView  = findViewById(R.id.waterrecycler)

        waterRecycler.setHasFixedSize(true)
        waterRecycler.layoutManager = GridLayoutManager(this, 2)
        val waterAdapter = RecylerAdapter(addWaterList(),this)
        waterRecycler.adapter = waterAdapter

        //sewer recycler
        var sewerRecycler: RecyclerView  = findViewById(R.id.sewerrecycler)
        sewerRecycler.setHasFixedSize(true)
        sewerRecycler.layoutManager = GridLayoutManager(this, 2)
        val sewerAdapter = RecylerAdapter(addSewerList(),this)
        sewerRecycler.adapter = sewerAdapter

        //Project recycler
        var projectRecycler: RecyclerView  = findViewById(R.id.projectrecycler)
        projectRecycler.setHasFixedSize(true)
        projectRecycler.layoutManager = GridLayoutManager(this, 2)
        val projectAdapter = RecylerAdapter(addProjectList(),this)
        projectRecycler.adapter = projectAdapter
    }

    private fun addWaterList():ArrayList<Params> {
        var paramList:ArrayList<Params> = ArrayList()
        paramList.add(Params(R.drawable.customermeters, "Customer Meters"))
        paramList.add(Params(R.drawable.waterpipes, "Water Pipes"))
        paramList.add(Params(R.drawable.watertanks, "Water Tanks"))
        paramList.add(Params(R.drawable.valves, "Valves"))
        paramList.add(Params(R.drawable.mastermeter, "Master Meters"))
        paramList.add(Params(R.drawable.prv, "PRV"))
        return paramList
    }

    private fun addSewerList():ArrayList<Params> {
        var paramList:ArrayList<Params> = ArrayList()
        paramList.add(Params(R.drawable.sewerlines, "Sewer Lines"))
        paramList.add(Params(R.drawable.manholes, "Manholes"))
        return paramList
    }

    private fun addProjectList():ArrayList<Params> {
        var paramList:ArrayList<Params> = ArrayList()
        paramList.add(Params(R.drawable.customermeters, "Point Project"))
        paramList.add(Params(R.drawable.waterpipes, "Line Project"))

        return paramList
    }

    private fun mappedWaterPipes() {
        startActivity(Intent(this@Home, MappedWaterPipes::class.java))
    }

    private fun mappedSewerLines() {
        startActivity(Intent(this@Home, MappedSewerLines::class.java))
    }

    private fun goHome() {
        startActivity(Intent(this@Home, Incidences::class.java))
    }

    private fun showUserInfo() {
        showUserDetails(userDetailsDialog)
    }

    //Change Password
    private fun changePassword() {
        showResetPasswordDialog(resetPasswordDialog)
    }

    //Display List of Water Meter Customers
    private fun mappedPoints() {
        startActivity(Intent(this@Home, MappedPoints::class.java))
    }

    //Sign Out User
    private fun signOut() {
        editor.remove("token")
        editor.commit()
        startActivity(Intent(this@Home, Incidences::class.java))
        finish()
    }

    //Dialog box for user to reset password
    private fun showResetPasswordDialog(d: Dialog) {
        userRenewPassword(d)

        val hide = d.findViewById<ConstraintLayout>(R.id.parent)
        hide.setOnClickListener {
            d.hide()
        }

        d.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        d.setCancelable(true);
        val window: Window = d.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        d.show()
        d.setCanceledOnTouchOutside(true);
    }

    //Logic to change user password
    private fun userRenewPassword(resetPasswordDialog: Dialog) {
        val oldPassword = resetPasswordDialog.findViewById<EditText>(R.id.oldPassword)
        val newPassword = resetPasswordDialog.findViewById<EditText>(R.id.newPassword)
        val error = resetPasswordDialog.findViewById<TextView>(R.id.error)
        val changePassword = resetPasswordDialog.findViewById<Button>(R.id.changePassword)

        changePassword.setOnClickListener {
            error.text = ""

            if (newPassword.text.toString().length < 6) {
                error.text = "New Password is too short!"
                return@setOnClickListener
            }

            val changePasswordBody = ChangePasswordBody(
                oldPassword.text.toString(),
                newPassword.text.toString(),
            )

            val jwt = JWT(preferences.getString("token", "")!!)
            val id = jwt.getClaim("UserID").asString()

            //Print id
            System.out.println("ID IS: $id")
            val apiInterface = ApiInterface.create().changePassword(id!!, changePasswordBody)


            apiInterface.enqueue(object : Callback<Message> {
                override fun onResponse(
                    call: Call<Message>?,
                    response: Response<Message>?
                ) {
                    //Print Response
                    System.out.println("RESPONSE IS: $response")

                    if (response?.body()?.success !== null) {
                        error.text = "Password updated successfuly"
                        editor.remove("token")
                        editor.commit()
                        error.text = "Logout successful!"
                        startActivity(Intent(this@Home, Incidences::class.java))
                    } else {
                        editor.putString("token", "")
                        editor.commit()
                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(
                    call: Call<ke.co.osl.umcollector.models.Message>?,
                    t: Throwable?
                ) {
                    System.out.println(t)
                    error.text = "Connection to server failed"
                    editor.putString("token", "")
                    editor.commit()
                }
            })
        }
    }

    //Logic to display user password
    private fun showUserDetails(d: Dialog) {
        val hide = d.findViewById<ConstraintLayout>(R.id.parent)
        val name = d.findViewById<TextView>(R.id.username)
        val email = d.findViewById<TextView>(R.id.useremail)
        val phone = d.findViewById<TextView>(R.id.userphone)
        val position = d.findViewById<TextView>(R.id.userposition)
        val role = d.findViewById<TextView>(R.id.userrole)
        val department = d.findViewById<TextView>(R.id.userdepartment)

        val jwt = JWT(preferences.getString("token", "")!!)

        name.text = jwt.getClaim("Name").asString()
        email.text = jwt.getClaim("Email").asString()
        phone.text = jwt.getClaim("Phone").asString()
        position.text = jwt.getClaim("Position").asString()
        role.text = jwt.getClaim("Role").asString()
        department.text = jwt.getClaim("Department").asString()

        hide.setOnClickListener {
            d.hide()
        }

        d.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        d.setCancelable(true)
        val window: Window = d.getWindow()!!
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        d.show()
        d.setCanceledOnTouchOutside(true)
    }

}