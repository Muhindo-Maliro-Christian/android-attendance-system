package com.example.attendancesystem.ui.employee

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.attendancesystem.R
import com.example.attendancesystem.application.MainApplication
import com.example.attendancesystem.models.employee.Employee
import com.example.attendancesystem.utils.adapters.RecyclerViewAdapter
import com.example.attendancesystem.utils.config.App
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import org.json.JSONTokener
import java.nio.charset.Charset

class EmployeeFragment : Fragment(), RecyclerViewAdapter.RowClickListener {

    companion object {
        fun newInstance() = EmployeeFragment()
    }

    private lateinit var viewModel: EmployeeViewModel
    private lateinit var recyclerView : RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var employeesList : List<Employee>
    private lateinit var swipeRefreshLayout : SwipeRefreshLayout

    private val employeeViewModel: EmployeeViewModel by viewModels {
        EmployeeViewModelFactory((activity?.application as MainApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.employee_fragment, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            recyclerViewAdapter = RecyclerViewAdapter(this@EmployeeFragment, requireContext(), requireActivity())
            adapter = recyclerViewAdapter

        }
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                layoutManager.orientation
            )
        )

        employeeViewModel.employees.observe(viewLifecycleOwner) {
            recyclerViewAdapter.setListData(ArrayList(it))
            employeesList = it
            if (employeesList.isEmpty()) {
                getEmployees()
            }
        }

        getEmployees()

        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout)
        swipeRefreshLayout.setOnRefreshListener {
            getEmployees()
        }
        return view

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(EmployeeViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onItemClickListener(employee: Employee) {
        val bundle = Bundle()
        bundle.putInt("id", employee.id)
        bundle.putString("name", employee.name)
        Navigation.findNavController(requireView()).navigate(R.id.action_employeeFragment_to_cameraFragment, bundle)
    }

    private fun getEmployees() {

        /*showSkeleton()*/


        val queue = Volley.newRequestQueue(context)
        val app =  App()
        Log.i("get", "${app.url}/employees")

        val getRequest: StringRequest = object : StringRequest(
            Method.GET, "${app.url}/employees",
            Response.Listener { response -> // response
                val gson = Gson()

                employeesList = gson.fromJson(response, object : TypeToken<List<Employee>>() {}.type)
                employeesList.forEach {
                    val blog = Employee(it.id, it.name, it.sexe, it.adresse, it.promotion, it.annee_academique)
                    employeeViewModel.insert(blog)
                }
                Log.i("get", employeesList.toString())

                swipeRefreshLayout.setRefreshing(false);

            },
            Response.ErrorListener { response -> // TODO Auto-generated method stub
                val strResponse = String(response.networkResponse?.data ?: ByteArray(0), Charset.forName(
                    HttpHeaderParser.parseCharset(response.networkResponse?.headers)))
                Log.i("get", strResponse)

                if(strResponse.isEmpty()){
                    Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
                }else{
                    if(response.networkResponse?.statusCode == 404){
                        Toast.makeText(context,"Url not found", Toast.LENGTH_LONG).show()
                    }else{
                        val jsonObject = JSONTokener(strResponse).nextValue() as JSONObject
                        val message = jsonObject.getString("message")
                        Log.i("message: ", message)
                        Toast.makeText(context,message, Toast.LENGTH_LONG).show()
                    }
                }
                swipeRefreshLayout.setRefreshing(false);

            }
        ){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
//                params["Authorization"] = "Bearer $token"
//                params["Serial-Number"] = serial_number
                return params
            }
        }
        queue.add(getRequest).retryPolicy = DefaultRetryPolicy(3 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0F)
    }


}