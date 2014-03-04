<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="../include/include.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>发布页面</title>
<%@ include file="../include/includeCss.jsp" %>
<style>
.table > thead {
	background-color: #eee;
}
.alert {
	font-size: 16px;
}
#logContent > br {
	font-size: 0px;
	line-height: 0px;
}
</style>
</head>
<body>
<%@ include file="../include/header.jsp" %>
<input type="hidden" id="J_deployType" value="${deployType}" />
<input type="hidden" id="J_version"  value="${version}" />
<input type="hidden" id="J_projectId" value="${project.id}" />
<input type="hidden" id="J_projectName" value="${project.name}" />
<input type="hidden" id="J_deployRecordId" value="${deployRecord.id}" />
<input type="hidden" id="J_patchGroupId" value="${patchGroup.id}" />
<div class="wrap">
	<h2 class="title">发布工程</h2>
	<div style="width: 490px; text-align: left; margin:30px auto 10px;">
		<table class="table table-bordered">
			<tbody>
				<tr>
					<td>发布工程:</td>
					<td>${project.name}</td>
				</tr>
				<tr>
					<td>版本号:</td>
					<td>${version}</td>
				</tr>
				<tr>
					<td>发布方式:</td>
					<td>${deployType}</td>
				</tr>
				<tr>
					<td>补丁分组:</td>
					<td>
					<c:choose>
						<c:when test="${patchGroup != null}">
							<a href="/patchGroup/detail/${patchGroup.id}" target="_blank">
							${patchGroup.name} (${patchGroup.checkCode})
							</a>
						</c:when>
						<c:otherwise>无</c:otherwise>
					</c:choose>
						
					</td>
				</tr>
			</tbody>
		</table>
	</div>
	<div style="width: 800px; margin: 20px auto 10px;">
		<table style="width: 800px; margin: 30px auto;">
			<tbody>
				<%-- 只有as-web需要无宕机选项 --%>
				<tr id="J_serverGroupWrap" <c:if test="${project.name != 'as-web'}">style="display: none;"</c:if>>
					<td style="width: 170px; font-size: 16px; padding-bottom: 10px;">
						<strong>无宕机选项:&nbsp;&nbsp;</strong>
					</td>
					<td>
						<select id="serverGroupSel" style="font-size: 16px;">
							<option value="ab" selected="selected">全部</option>
							<option value="a">a组</option>
							<option value="b">b组</option>
						</select>
					</td>
				</tr>
				<tr>
					<td style="font-size: 16px; padding-bottom: 10px;">
						<strong>上传文件:&nbsp;&nbsp;</strong>
					</td>
					<td>
						<div style="display: inline-block;" id="J_fileUploadWidget"></div>
						<div style="display:inline-block;">
							<button type="button" id="J_uploadBtn" class="btn btn-primary" style="width: 80px; margin-bottom: 10px;">上&nbsp;&nbsp;传</button>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
		
		<!-- 显示补丁或war的上传结果 -->
		<div id="J_uploadResultWrap" style="text-align: center;"></div>
	
		<div style="text-align: center;">
			<button type="button" class="btn btn-primary" id="decompressBtn">解压补丁文件</button>
		</div>
		
		<!-- 文件列表 -->
		<div id="fileListWrap">
			<h3>文件列表</h3>
			<table id="fileListTbl" class="table table-bordered table-condensed table-hover table-striped" style="width: 800px;;">
				<thead>
					<tr><th>文件路径</th></tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
		
		
		<!-- 文件冲突列表 -->
		<div id="conflictFileInfoWrap">
			<h3>冲突详情</h3>
			<table id="conflictFileInfoTbl" class="table table-bordered table-condensed table-hover table-striped" style="width: 800px;">
				<thead>
					<tr>
						<th width="600">文件路径</th>
						<th width="200">冲突补丁组</th>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
		
		<!-- 发布按钮 -->
		<div id="deployBtnWrap" style="text-align: center;">
			<button type="button" class="btn btn-primary" style="width:100px; margin: 0px 10px;" id="startDeployBtn">发&nbsp;&nbsp;布</button>
			<button type="button" class="btn btn-primary" style="width:100px; margin: 0px 10px;" id="startRollbackBtn">回&nbsp;&nbsp;滚</button>
			<div style="margin-top: 20px;" id="deployStatus"></div>
		</div>
		
		<!-- 文件冲突列表 -->
		<h3>日志</h3>
		<pre id="logContent" style="width: 781px; height: 400px; overflow: auto; font-size: 15px;"></pre>
		<div style="text-align: center; margin: 30px auto;">
			<button type="button" class="btn btn-primary" id="J_unlockAndLeave">解锁并返回首页</button>
		</div>
	</div>
</div>
</body>
<%@ include file="../include/includeJs.jsp" %>
<script type="text/javascript" src="${ctx_path}/js/bootstrap/bootstrapFileInput.js"></script>
<script type="text/javascript" src="${ctx_path}/js/jquery/ajaxfileupload.js"></script>
<script>
$(function(){
	initOnBeforeUnload();
	initFileUploadWidget();
	initUnlockAndLeaveBtn();
});
function initFileUploadWidget(){
	var projectName = $('#J_projectName').val(),
		deployType = $('#J_deployType').val(),
		version = $('#J_version').val(),
		projectId = $('#J_projectId').val(),
		deployRecordId = $('#J_deployRecordId').val(),
		patchGroupId = $('#J_patchGroupId').val() || 0;
	
	$('#J_fileUploadWidget').bootstrapFileInput({
		width: '500px',
		btnWidth: '80px',
		fileInputId: 'J_deployItemField',
		fileInputName: 'deployItemField'
	});
	$('#J_uploadBtn').on('click', function(){
		var $this = $(this);
		var deployItemName = $('#J_deployItemField').val();
		if(!deployItemName){
			alert('请先选择要上传的文件!');
			return false;
		}
		// static的情形只能发版本，上传tar.gz包
		if( projectName != 'as-static') {
			if(deployType == 'patch' && !(/.zip$/i).test(deployItemName)){
				alert('请选择zip压缩格式的补丁文件!');
				return false;
			}
			if(deployType == 'war' && !(/.war$/i).test(deployItemName)){
				alert('请选择war包进行上传!');
				return false;
			}
		} else {
			if(!(/.tar.gz/i).test(deployItemName)) {
				alert('请注意，【static】工程只能上传tar.gz包发版本!!!\请不要发补丁!!!');
				return false;
			}
		}
		$this.html('上传中').attr({disabled: true});
		var $uploadResultWrap = $('#J_uploadResultWrap');
		$.ajaxFileUpload({
			url: '/deploy/uploadItem',
			secureuri: false, 
			fileElementId:'J_deployItemField',
			dataType: 'json',
			data: {
				projectId: projectId,
				version: version,
				deployType: deployType,
				deployRecordId: deployRecordId,
				patchGroupId: patchGroupId
			},
			success: function (data, status){
				if(data.success === true){
					var sizeUnits = ['byte', 'kb', 'MB', 'GB']
					var size = data.size;
					for(i=0; i <=sizeUnits.length && size > 1024; size = (size/1024).toFixed(2), i++);
					var sizeStr = size + sizeUnits[i];
					showAlert($uploadResultWrap, [
						'文件上传成功!',
						'文  件  名: <strong>' + data.filename + '</strong>',
						'文件大小: <strong>' + sizeStr + '</strong>'
					].join('<br/>'), 'success');
				}else{
					this.error(data, status);
					return;
				}
				$this.html('上&nbsp;&nbsp;传').attr({disabled: false});
			},
			error: function(data, status, e){
				showAlert($uploadResultWrap, data.message || '文件上传失败!', 'error');
				$this.html('上&nbsp;&nbsp;传').attr({disabled: false});
			}
		});
		return false;
	});
}
function initUnlockAndLeaveBtn() {
	$('#J_unlockAndLeave').on('click', function(){
		if(!confirm('确认要解锁本次发布并离开?')){
			return;
		}
		window.onbeforeunload = null;
		location.href = CTX_PATH + '/deploy/unlockDeployRedirect';
	});
}
function initOnBeforeUnload() {
	window.onbeforeunload = function(){
		var alarmStr = '发布过程中，请不要离开!\n请点击 [ 取消 ] 或 [ 留在此页 ] ';
		var rmozilla = /(mozilla)(?:.*? rv:([\w.]+))?/;
		if(rmozilla.test(navigator.userAgent) === true && !confirm(alarmStr)){
			return false;
		}
		window.event.returnValue = alarmStr;
		return alarmStr;
	};
}
function showAlert(wrap, msg, status, closable){
	var $wrap = $.type(wrap) == 'string'? $('#' + wrap): wrap;
	if($wrap.size() == 0) {
		return;
	}
	$wrap.empty();
	var $alert = $('<div class="alert">');
	$alert.html(msg);
	if(closable !== false) {
		$alert.prepend('<button type="button" class="close" data-dismiss="alert">&times;</button>');
	}
	if(status) {
		$alert.addClass('alert-' + status);
	}
	$wrap.append($alert);
}
</script>
</html>