package com.ablesky.asdeploy.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ablesky.asdeploy.dao.base.QueryParamMap;
import com.ablesky.asdeploy.pojo.ConflictDetail;
import com.ablesky.asdeploy.pojo.DeployItem;
import com.ablesky.asdeploy.pojo.DeployRecord;
import com.ablesky.asdeploy.service.IDeployService;
import com.ablesky.asdeploy.service.IProjectService;
import com.ablesky.asdeploy.util.CommonConstant;
import com.ablesky.asdeploy.util.DeployUtil;

@Controller
@RequestMapping("/deployRecord")
public class DeployRecordController {
	
	@Autowired
	private IProjectService projectService;
	@Autowired
	private IDeployService deployService;
	
	@RequestMapping("/list")
	public String list(
			@RequestParam(defaultValue=CommonConstant.DEFAUTL_START_STR)
			Integer start,
			@RequestParam(defaultValue=CommonConstant.DEFAULT_LIMIT_STR)
			Integer limit,
			@RequestParam(required=false)
			String username,
			@RequestParam(defaultValue="0")
			Long projectId,
			@RequestParam(required=false)
			String deployType,
			@RequestParam(required=false)
			String version,
			Model model) {
		if(start == null) {
			start = CommonConstant.DEFAUTL_START;
		}
		if(limit == null) {
			limit = CommonConstant.DEFAULT_LIMIT;
		}
		
		QueryParamMap paramMap = new QueryParamMap()
				.addParam(StringUtils.isNotBlank(username), "user_username__contain", username)
				.addParam(projectId != null && projectId > 0, "project_id", projectId)
				.addParam(StringUtils.isNotBlank(deployType), "deployItem_deployType", deployType)
				.addParam(StringUtils.isNotBlank(version), "deployItem_version", version)
				.orderByDesc("id");
		
		model.addAttribute("projectList", projectService.getProjectListResult(0, 0, QueryParamMap.EMPTY_MAP));
		model.addAttribute("page", deployService.getDeployRecordPaginateResult(start, limit, paramMap));
		
		return "deployRecord/list";
	}
	
	@RequestMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		DeployRecord deployRecord = deployService.getDeployRecordById(id);
		DeployItem deployItem = deployRecord.getDeployItem();
		List<String> filePathList = Collections.emptyList();
		String readme = "";
		if(deployItem != null) {
			String targetFolderPath = FilenameUtils.concat(deployItem.getFolderPath(), FilenameUtils.getBaseName(deployItem.getFileName()));
			filePathList = DeployUtil.getDeployItemFilePathList(targetFolderPath);
			readme = DeployUtil.loadReadmeContent(targetFolderPath);
		}
		List<ConflictDetail> conflictDetailList = deployRecord.getIsConflictWithOthers()
				? deployService.getConflictDetailListResultByParam(new QueryParamMap().addParam("deployRecordId", id))
				: Collections.<ConflictDetail>emptyList();
		
		Collections.sort(conflictDetailList, new Comparator<ConflictDetail>() {
			@Override
			public int compare(ConflictDetail detail1, ConflictDetail detail2) {
				Long patchGroupId1 = detail1.getConflictInfo().getRelatedPatchGroupId();
				Long patchGroupId2 = detail2.getConflictInfo().getRelatedPatchGroupId();
				if(patchGroupId1.equals(patchGroupId2)) {
					String filePath1 = detail1.getConflictInfo().getPatchFile().getFilePath();
					String filePath2 = detail2.getConflictInfo().getPatchFile().getFilePath();
					return filePath1.compareTo(filePath2);
				}
				return patchGroupId1.compareTo(patchGroupId2);
			}
		});
		List<String> originFilePathList = new ArrayList<String>();
		List<String> optimizedFilePathList = new ArrayList<String>();
		for(String filePath: filePathList) {
			if(filePath.contains("_optimize")) {
				optimizedFilePathList.add(filePath);
			} else {
				originFilePathList.add(filePath);
			}
		}
		Collections.sort(originFilePathList);
		Collections.sort(optimizedFilePathList);
		
		model.addAttribute("deployRecord", deployRecord)
			.addAttribute("originFilePathList", originFilePathList)
			.addAttribute("optimizedFilePathList", optimizedFilePathList)
			.addAttribute("readme", readme)
			.addAttribute("conflictDetailList", conflictDetailList);
		return "deployRecord/detail";
	}
	

}
