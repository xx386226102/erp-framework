package com.chenyanwu.erp.erpframework.controller.importutil;

import com.chenyanwu.erp.erpframework.common.PageResultBean;
import com.chenyanwu.erp.erpframework.common.ResultBean;
import com.chenyanwu.erp.erpframework.common.util.ExcelUtil;
import com.chenyanwu.erp.erpframework.entity.importutil.StudentExcelImport;
import com.chenyanwu.erp.erpframework.entity.rbac.ErpRole;
import com.chenyanwu.erp.erpframework.exception.ExceptionEnum;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.chenyanwu.erp.erpframework.service.importutil.ErpStudentService;
import com.chenyanwu.erp.erpframework.entity.importutil.ErpStudent;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author chenyanwu
 * @date 2019-04-26 12:37:18
 */
@Controller

@RequestMapping(value = "/erpstudent")
public class ErpStudentController {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ErpStudentService service;


    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public ResultBean<ErpStudent> get(String id) {
        ErpStudent item = (ErpStudent) service.selectByPrimaryKey(id);
        if (item != null) {
            return new ResultBean<>(item);
        } else {
            return new ResultBean<>(ExceptionEnum.RESOURCE_NOT_FOUND, null, "找不到该记录", null);
        }
    }


    @RequestMapping(value = "/getlist", method = RequestMethod.POST)
    @ResponseBody
    public PageResultBean<List<ErpStudent>> getList(int page, int limit, String keyword) {
        List<ErpStudent> list;
        PageHelper.startPage(page, limit);
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = "%" + keyword.trim() + "%";
            Example example = new Example(ErpStudent.class);
            example.createCriteria().andLike("name", keyword);
            list = service.selectByExample(example);
        } else {
            list = service.selectAll();
        }
        return new PageResultBean(list, page, limit, ((Page) list).getTotal());

    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ResultBean<String> create(@Validated ErpStudent item) {
        service.insertSelective(item);
        return new ResultBean<String>("");
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public ResultBean<String> update(@Validated ErpStudent item) {
        service.updateByPrimaryKeySelective(item);
        return new ResultBean<String>("");
    }

    @RequestMapping(value = "/deleteByID", method = RequestMethod.POST)
    @ResponseBody
    public ResultBean<Integer> delete(String id) {
        int result = service.deleteByPrimaryKey(id);
        return new ResultBean<Integer>(result);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ResultBean<Integer> delete(ErpStudent item) {
        int result = service.delete(item);
        return new ResultBean<Integer>(result);
    }

    @GetMapping("index")
    public ModelAndView index(ModelAndView modelAndView) {
        modelAndView.setViewName("/import/index");
        return modelAndView;
    }

    @GetMapping("add")
    public ModelAndView add() {
        return new ModelAndView();
    }

    @GetMapping("edit")
    public ModelAndView edit(String id) {
        ModelAndView modelAndView = new ModelAndView();
        ErpStudent item = (ErpStudent) service.selectByPrimaryKey(id);
        modelAndView.addObject("entity", item);
        return modelAndView;
    }

    //生成模板
    @RequestMapping(value = "export2excel")
    public void export2excel(HttpServletResponse response) {
        List<StudentExcelImport> excellist = new ArrayList<>();
        //模拟从数据库获取需要导出的数据
        ExcelUtil.exportExcel(excellist, "学生信息导入表", "sheet", StudentExcelImport.class, "学生信息.xls", response);
    }

    @RequestMapping(value = "/import", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView exceclImport(ModelAndView modelAndView) {
        modelAndView.setViewName("/import/import");
        return modelAndView;
    }

    @RequestMapping("excelImport")
    @ResponseBody
    public ResultBean<Integer> importExcel(@RequestParam("file") MultipartFile file) {
        List<StudentExcelImport> list = ExcelUtil.importExcel(file, 1, 1, StudentExcelImport.class);
        //验证数据，记录不对的数据，并把正确的数据插入deposit表
        int errorCounts = service.importDataReturn(list);
        return new ResultBean<>(errorCounts);
    }

    //下载本次导入错误数据
    @RequestMapping(value = "exportErrol2excel")
    public void exportErrol2excel(HttpServletResponse response) {
        List<StudentExcelImport> excellist = service.findStudentErrorExcel();
        //模拟从数据库获取需要导出的数据
        ExcelUtil.exportExcel(excellist, "学生信息导入表-错误数据", "sheet", StudentExcelImport.class, "学生信息-errorData" + ".xls", response);

    }
}