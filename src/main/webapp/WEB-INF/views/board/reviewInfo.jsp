<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Gamja+Flower&display=swap" rel="stylesheet">
<style>
.Wtitle{
font-family: 'Gamja Flower', cursive;
font-size: 64px;
text-align: center;
}
</style>
<script>
$(document).ready(function(){
	$("#btnList").on("click", function(){
		document.bbsForm.action = "/board/reviews";
		document.bbsForm.submit();
	});
	
	
	<c:if test="${boardMe eq 'Y'}">
	$("#btnUpdate").on("click", function(){
		document.bbsForm.action = "/board/fUpdateForm";
		document.bbsForm.submit();
	});
	
	$("#btnDelete").on("click", function(){
		if(confirm("정말 삭제 하시겠습니까?") == true)
		{
			//정말 삭제하겠다고 했을 때, ajax 통신
			$.ajax({
				type:"POST",
				url:"/board/reviewDelete",
				data:
				{
					bSeq: <c:out value="${wdReview.RSeq}" />
				},
				datatype:"JSON",
				beforeSend:function(xhr){
					xhr.setRequestHeader("AJAX", "true");
				},
				success:function(response){
					if(response.code == 0)
					{
						alert("게시물이 삭제되었습니다.");
						location.href = "/board/reviews";
					}
					else if(response.code == 400)
					{
						alert("로그인이 되어있지 않습니다.");
						location.href = "/board/reviews";
						
					}
					else if(response.code == 404)
					{
						alert("게시물을 찾을 수 없습니다.");
						location.href = "/board/reviews";
					}
					else if(response.code == 405)
					{
						alert("사용자의 게시물이 아닙니다.");
						location.href = "/board/reviews";
					}
					else
					{
						alert("게시물 삭제 중 오류가 발생했습니다.");
					}
				},
				complete:function(data){
					icia.common.log(data);
				},
				error:function(xhr, status, error)
				{
					icia.common.error(error);
				}
			});
			
		}
	});	
	</c:if>
    
});
</script>
</head>
<body>
   	<jsp:include page="/WEB-INF/views/include/navigation.jsp" >
       <jsp:param name="userName" value="${wdUser.userNickname}" />
       </jsp:include>
       
    <div class="page-heading-rent-venue">
        <div class="container">
            <div class="row">
            </div>
        </div>
    </div>
    <br />
    <h2 class="Wtitle">Review</h2>
    <p style="text-align:center">여러분들의 노하우를 공유해보세요</p>
    <br />

<div class="container">
   <div class="row" style="margin-right:0; margin-left:0;">
   	  <div class="col-lg-12">
      <table class="table">
         <thead>
            <tr class="table-active dongdong">
               <td style="width:60%">
                  <c:out value="${wdReview.RTitle}"/>
               </td>
               <td style="width:40%" class="text-right">
                                         조회 : <fmt:formatNumber type="number" maxFractionDigits="3" value="${wdReview.RReadCnt}" />
               </td>
            </tr>
            <tr>
               <td style="width:60%">
               	작성자 : <c:out value="${wdReview.UNickName}"/>
               </td>
               <td style="width:40%" class="text-right">
                  <div>${wdReview.regDate}</div>
               </td>
            </tr>   

         </thead>
         <tbody>
             <tr>
             <!-- 첨부파일 다운 안해요 -->
            </tr>
            <tr>
               <td colspan="2" style="text-align:center">
	               <div style="padding:10px">
	               	<div>
	               	<img src="../resources/upload/${url }">
	               		<br>
	               		<c:out value="${wdReview.RContent}" />
	               	</div>
	               </div>
               </td>
            </tr>
            <div></div>
         </tbody>
         <tfoot>
         <tr>
         <td colspan="2">
         
         </td>
         </tr>
         <tr>
               	<td colspan="2">
         <c:if test="${boardMe eq 'Y'}">
               		<button type="button" id="btnDelete" class="w-btn w-btn-red">삭제</button>
         </c:if>
               		<button type="button" id="btnList" class="w-btn w-btn-green2" style="float: right">리스트</button>
         <c:if test="${boardMe eq 'Y'}">
               		<button type="button" id="btnUpdate" class="w-btn w-btn-green" style="margin-right: 10px;">수정</button>
         </c:if>      	
               	</td>
         </tr>
         </tfoot>
      </table>
      </div>
   </div>
</div>


<form name="bbsForm" id="bbsForm" method="post">
   <input type="hidden" name="RSeq" value="${RSeq}" />
   <input type="hidden" name="searchValue" value="${searchValue}" />
   <input type="hidden" name="curPage" value="${curPage}" />
</form>

 <!-- *** 욱채수정Footer 시작 *** -->
	<%@ include file="/WEB-INF/views/include/footer.jsp" %>
 <!-- *** 욱채수정Footer 종료 *** -->
</body>
</html>