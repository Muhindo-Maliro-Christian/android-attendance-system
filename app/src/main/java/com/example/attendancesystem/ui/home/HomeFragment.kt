package com.example.attendancesystem.ui.home

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.attendancesystem.R
import com.example.attendancesystem.models.employee.Employee
import com.example.attendancesystem.utils.adapters.RecyclerViewAdapter

class HomeFragment : Fragment()  {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false)
        val text: TextView = view.findViewById(R.id.main_text)
        val paint = text.paint
        val width = paint.measureText(text.text.toString())
        val textShader: Shader = LinearGradient(0f, 0f, width, text.textSize, intArrayOf(
            Color.parseColor("#3F91EC"),
            Color.parseColor("#020E4E"),
        ), null, Shader.TileMode.REPEAT)

        text.paint.setShader(textShader)

        view.findViewById<Button>(R.id.btnAddAttendance).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_cameraFragment)
        }

        view.findViewById<Button>(R.id.EnrollButton).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_employeeFragment)
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }



}