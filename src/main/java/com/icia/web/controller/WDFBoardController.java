package com.icia.web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.icia.common.model.FileData;
import com.icia.common.util.FileUtil;
import com.icia.common.util.StringUtil;
import com.icia.web.model.Paging;
import com.icia.web.model.Response;
import com.icia.web.model.WDBoardFile;
import com.icia.web.model.WDComment;
import com.icia.web.model.WDFBoard;
import com.icia.web.model.WDUser;
import com.icia.web.service.WDCommentService;
import com.icia.web.service.WDFBoardService;
import com.icia.web.service.WDUserService;
import com.icia.web.util.CookieUtil;
import com.icia.web.util.HttpUtil;

@Controller("wdFBoardController")
public class WDFBoardController 
{
	private static Logger logger = LoggerFactory.getLogger(WDFBoardController.class);
	
	@Value("#{env['auth.cookie.name']}")
	private String AUTH_COOKIE_NAME;
	
	@Autowired
	private WDFBoardService wdFBoardService;
	
	@Autowired
	private WDUserService wdUserService;
	
	@Autowired
	private WDCommentService wdCommentService;
	
	//파일 저장경로
	@Value("#{env['upload.save.dir']}")
	private String UPLOAD_SAVE_DIR;
	
	private static final int LIST_COUNT = 20;
	private static final int PAGE_COUNT = 5;
	
	@RequestMapping(value="/board/fBoard")
	public String list(ModelMap model, HttpServletRequest request, HttpServletResponse response) 
	{
		String searchType = HttpUtil.get(request, "searchType", "");
		String searchValue = HttpUtil.get(request, "searchValue", "");
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		
		WDUser wdUser = wdUserService.userSelect(cookieUserId);
		
		long totalCount = 0;
		List<WDFBoard> list = null;
		
		//페이징 객체
		Paging paging = null;
		
		WDFBoard search = new WDFBoard();
		
		if(!StringUtil.isEmpty(searchType) && !StringUtil.isEmpty(searchValue)) 
		{
			//받아온 값
			search.setSearchType(searchType);
			search.setSearchValue(searchValue);
		}
		else 
		{
			searchType = "";
			searchValue = "";
		}
		
		//페이징 처리를 위한 totalCount, 그리고 값이 있어야 가져오니까, 풀스캔 안하는 용도도 있음.
		totalCount = wdFBoardService.fBoardListCount(search);
		
		logger.debug("[totalCount] = "+totalCount);
		
		ArrayList<Integer> commentcount = new ArrayList<Integer>();
		
		if(totalCount > 0) 
		{
			paging = new Paging("/board/fboard", totalCount, LIST_COUNT, PAGE_COUNT, curPage, "curPage");
			paging.addParam("searchType", searchType);
			paging.addParam("searchValue", searchValue);
			paging.addParam("curPage", curPage);
			
			search.setStartRow(paging.getStartRow());
			search.setEndRow(paging.getEndRow());
			
			list = wdFBoardService.fBoardList(search);
			for(int i = 0; i<list.size();i++) {
				commentcount.add(i, wdCommentService.commentListCount(list.get(i).getbSeq()));
			}
			model.addAttribute("commentcount",commentcount);
		}
		
		model.addAttribute("list", list);
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchValue", searchValue);
		model.addAttribute("curPage", curPage);
		model.addAttribute("paging", paging);
		model.addAttribute("wdUser", wdUser);
		
		return "/board/fBoard";
	}
	
	
	//작성페이지
	@RequestMapping(value="/board/fBoardWrite", method=RequestMethod.POST)
	public String fBoardWrite(ModelMap model, HttpServletRequest request, HttpServletResponse response) 
	{
		//쿠키 값
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		//글 쓰고 돌아갈 때 서치타입 벨류 현재페이지 세팅이 필요함
		String searchType = HttpUtil.get(request, "searchType", "");
		String searchValue = HttpUtil.get(request, "searchValue", "");
		long curPage = HttpUtil.get(request, "curPage", (long)1);		
		
		WDUser wdUser = wdUserService.userSelect(cookieUserId);
		
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchValue", searchValue);
		model.addAttribute("curPage", curPage);
		
		model.addAttribute("wdUser", wdUser);
		
		return "/board/fBoardWrite";
	}
	
	
	
	//상세 뷰페이지
	@RequestMapping(value="/board/fBoardView")
	public String fBoardView(ModelMap model, HttpServletRequest request, HttpServletResponse response) 
	{
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		
		long bSeq = HttpUtil.get(request, "bSeq", (long)0);
		String searchType = HttpUtil.get(request, "searchType", "");
		String searchValue = HttpUtil.get(request, "searchValue", "");
		long curPage = HttpUtil.get(request, "curPage", (long)1);
		
		String boardMe = "N";
		WDFBoard wdFBoard = null;
		
		//댓글 리스트 객체
		List<WDComment> commentList = null;
		WDComment wdComment = new WDComment();
		
		int maxComment = 0;
		if(bSeq > 0) 
		{
			
			wdFBoard = wdFBoardService.wdFBoardView(bSeq);
			
			if(wdFBoard != null && StringUtil.equals(wdFBoard.getUserId(), cookieUserId)) 
			{
				boardMe = "Y";
			}
			commentList = wdCommentService.commentList(bSeq);
			maxComment = wdCommentService.WDCommentMax(bSeq);
			
			model.addAttribute("commentList",commentList);
			model.addAttribute("maxComment",maxComment);
			
			
		}
		
		if(wdFBoard.getWdBoardFile() != null) 
		{
			String url = wdFBoard.getWdBoardFile().getFileName();
			model.addAttribute("url", url);
		}
		
		WDUser wdUser = wdUserService.userSelect(cookieUserId);
		
		String content = wdFBoard.getbContent().replaceAll("<br>", "\r\n");
		wdFBoard.setbContent(content);
		
		model.addAttribute("wdUser", wdUser);
		model.addAttribute("cookieUserId",cookieUserId);
		model.addAttribute("bSeq", bSeq);
		model.addAttribute("searchType", searchType);
		model.addAttribute("searchValue", searchValue);
		model.addAttribute("curPage", curPage);
		model.addAttribute("wdFBoard", wdFBoard);
		model.addAttribute("boardMe", boardMe);
		
		return "/board/fBoardView";
	}
	
	
	
	//게시물 작성
	@RequestMapping(value="/board/writeProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> writeProc(MultipartHttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		String hiBbsTitle = HttpUtil.get(request, "hiBbsTitle", "");
		String hiBbsContent = HttpUtil.get(request, "hiBbsContent", "");
		
		
		FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);
		
		if(!StringUtil.isEmpty(hiBbsTitle) && !StringUtil.isEmpty(hiBbsContent)) 
		{
			WDFBoard wdFBoard = new WDFBoard();
			wdFBoard.setUserId(cookieUserId);
			wdFBoard.setbTitle(hiBbsTitle);
			wdFBoard.setbContent(hiBbsContent);
			
			if(fileData != null && fileData.getFileSize() >0 ) 
			{
				WDBoardFile wdBoardFile = new WDBoardFile();
				
				wdBoardFile.setFileName(fileData.getFileName());
				wdBoardFile.setFileOrgName(fileData.getFileOrgName());
				wdBoardFile.setFileExt(fileData.getFileExt());
				wdBoardFile.setFileSize(fileData.getFileSize());
				
				wdFBoard.setWdBoardFile(wdBoardFile);
			}
			
			try 
			{
				if(wdFBoardService.boardInsert(wdFBoard) > 0) 
				{
					ajaxResponse.setResponse(0, "Success");
				}
				else 
				{
					ajaxResponse.setResponse(500, "Internal Server Error");
				}
			}
			catch(Exception e) 
			{
				logger.error("[WDFBoardController] /board/writeProc Exception", e);
				ajaxResponse.setResponse(500, "Internal Server Error");
			}
		
		}
		else 
		{
			ajaxResponse.setResponse(400, "Bad Request: No parameter");
		}
		
		return ajaxResponse;
	}
	
	
	//게시글 삭제
	@RequestMapping(value="/board/delete", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> delete(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String cookieUserId =  CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		long bSeq = HttpUtil.get(request, "bSeq", (long)0);
		
		System.out.println("bSeq"+ bSeq);
		
		
		if(bSeq > 0) 
		{
			WDFBoard wdFBoard = wdFBoardService.wdFBoardView(bSeq);
			if(wdFBoard != null) 
			{
				
				if(StringUtil.equals(wdFBoard.getUserId(), cookieUserId)) 
				{
					try 
					{
						if(wdFBoardService.fBoardDelete(bSeq) > 0) 
						{
							wdCommentService.commentBoardDelete(bSeq);
							ajaxResponse.setResponse(0, "Success");
						}
						else 
						{
							ajaxResponse.setResponse(500, "Internal Server Error");
						}
					}
					catch(Exception e) 
					{
						logger.error("[WDFBoardController] delete Exception", e);
						ajaxResponse.setResponse(500, "Internal Server Error");
					}
				}
				else 
				{
					ajaxResponse.setResponse(405, "User Error");					
				}
			}
			else 
			{
				ajaxResponse.setResponse(404, "Not Exist");				
			}
			
		}
		else 
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		
		return ajaxResponse;
	}
	
	//첨부파일 다운로드
	@RequestMapping("/board/download")
	public ModelAndView download(HttpServletRequest request, HttpServletResponse response) 
	{
		ModelAndView modelAndView = null;
		
		long bSeq = HttpUtil.get(request, "bSeq", (long)0);
		
		if(bSeq >0) 
		{
			//값이 넘어왔다는 뜻
			WDBoardFile wdBoardFile = wdFBoardService.fBoardFileSelect(bSeq);
			
			if(wdBoardFile != null && wdBoardFile.getFileSize() > 0)
			{
				//파일이 존재하면
				//자바에서 제공하는 File객체라서 java.io임
				File file = new File(UPLOAD_SAVE_DIR + FileUtil.getFileSeparator()+wdBoardFile.getFileName());
				
				logger.debug("UPLOAD_SAVE_DIR : "+ UPLOAD_SAVE_DIR);
				//os버전에 따라서 슬래쉬 혹은 역슬래쉬 해주는 기능이 getFIleSeparator()임
				logger.debug("FileUtil.getFileSeparator() : "+ FileUtil.getFileSeparator());
				logger.debug("wdBoardFile.getFileName() : "+ wdBoardFile.getFileName());

				if(FileUtil.isFile(file)) 
				{
					//파일유틸에서 파일이 존재하는지 확인
					modelAndView = new ModelAndView();
					
					//어떤 클래스를 참조할 것이냐?
					modelAndView.setViewName("fileDownloadView"); //servlet-context.xml 에서 정의한 것(fileDownloadView)
					
					
					modelAndView.addObject("file", file);
					modelAndView.addObject("fileName", wdBoardFile.getFileOrgName());
					
					return modelAndView;
				}
			}
		}
		
		return modelAndView;
	}
	
	//게시물 댓글 등록
	   @RequestMapping(value="/board/CommentProc", method=RequestMethod.POST)
	   @ResponseBody
	   public Response<Object> CommentProc(HttpServletRequest request, HttpServletResponse response){
		   Response<Object> ajaxResponse = new Response<Object>();
		   String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		   long bSeq = HttpUtil.get(request, "bSeq", (long)0);
		   String wdFBoardComment = HttpUtil.get(request, "wdFBoardComment", "");
		   
		   WDComment wdComment = new WDComment();
		   WDUser wdUser = null;

		   if(bSeq > 0 && !StringUtil.isEmpty(wdFBoardComment)) {
			   WDFBoard wdFBoard = wdFBoardService.wdFBoardView(bSeq);
			   if(wdFBoard != null) 
			   {
				   
				   int maxcomment = wdCommentService.WDCommentMax(wdFBoard.getbSeq());
				   wdUser = wdUserService.userSelect(cookieUserId);
				   
				   
				   if(wdUser !=null) 
				   {
					   wdComment.setParentSeq(wdFBoard.getbSeq());
					   wdComment.setCommentSeq(maxcomment+1);
					   wdComment.setUserId(cookieUserId);
					   wdComment.setuNickName(wdUser.getUserNickname());
					   wdComment.setuEmail(wdUser.getUserEmail());
					   wdComment.setWdFBoardComment(wdFBoardComment);
					   
					   if(wdCommentService.WDCommentInsert(wdComment) > 0) {
						   ajaxResponse.setResponse(0, "Success");
					   }
					   else {
						   ajaxResponse.setResponse(500, "Internal Server Error");
					   }					   
				   }
				   else 
				   {
					   ajaxResponse.setResponse(406, "Not Found");
				   }
				   
			   }
			   else {
				   	//계정이 없음
		            ajaxResponse.setResponse(404, "Not Found");
			   }
		   }
		   else {
			   //파라미터값 잘못된 경우
			   ajaxResponse.setResponse(400, "Bad Request");
		   }
		   
		   return ajaxResponse;
	   }	
	
	   
	   //게시물 수정 페이지
	   @RequestMapping(value="/board/fUpdateForm")
	   public String fUpdateForm(ModelMap model, HttpServletRequest request, HttpServletResponse response) 
	   {
		   String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
		   
		   long bSeq = HttpUtil.get(request, "bSeq", (long)0);
		   String searchType = HttpUtil.get(request, "searchType", "");
		   String searchValue = HttpUtil.get(request, "searchValue", "");
		   long curPage = HttpUtil.get(request, "curPage", (long)1);

		   WDFBoard wdFBoard = null;
		   WDUser wdUser = null;
		   
		   if(bSeq > 0) 
		   {
			   wdFBoard = wdFBoardService.wdFBoardView(bSeq);
			   
			   if(wdFBoard != null) 
			   {
				   if(!StringUtil.equals(cookieUserId, wdFBoard.getUserId())) 
				   {
					   wdFBoard = null;
				   }
				   else 
				   {
					   wdUser = wdUserService.userSelect(cookieUserId);
				   }
			   }
		   }
		   
		   model.addAttribute("searchType", searchType);
		   model.addAttribute("searchvalue", searchValue);
		   model.addAttribute("curPage", curPage);
		   model.addAttribute("wdFBoard", wdFBoard);
		   model.addAttribute("wdUser", wdUser);
		   
		   return "/board/fUpdateForm";
	   }
	   
	   	//게시물 수정
		@RequestMapping(value="/board/updateProc", method=RequestMethod.POST)
		@ResponseBody
		public Response<Object> updateProc(MultipartHttpServletRequest request, HttpServletResponse response)
		{
			Response<Object> ajaxResponse = new Response<Object>();
			
			String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
			
			long bSeq = HttpUtil.get(request, "bSeq", (long)0);
			String bTitle = HttpUtil.get(request, "bTitle", "");
			String bContent = HttpUtil.get(request, "bContent", "");
			
			FileData fileData = HttpUtil.getFile(request, "hiBbsFile", UPLOAD_SAVE_DIR);
			WDFBoard wdFBoard = null;
			
			if(bSeq >0 && !StringUtil.isEmpty(bTitle) && !StringUtil.isEmpty(bContent)) 
			{
				//게시글 존재, 제목, 내용도 넘어옴.
				wdFBoard = wdFBoardService.wdFBoardView(bSeq);
				if(wdFBoard != null) 
				{
					if(StringUtil.equals(cookieUserId, wdFBoard.getUserId())) 
					{
						//게시물 작성자 아이디와 쿠키 아이디가 같음
						//얘는 변경이 없지만 해두자
						wdFBoard.setbSeq(bSeq);
						wdFBoard.setbTitle(bTitle);
						wdFBoard.setbContent(bContent);
						if(fileData != null && fileData.getFileSize()>0) 
						{
							//파일 존재
							WDBoardFile wdBoardFile = new WDBoardFile();

							//새로운 값들을 넣어줌
							wdBoardFile.setFileName(fileData.getFileName());
							wdBoardFile.setFileOrgName(fileData.getFileOrgName());
							wdBoardFile.setFileExt(fileData.getFileExt());
							wdBoardFile.setFileSize(fileData.getFileSize());
							
							wdFBoard.setWdBoardFile(wdBoardFile);
						}
						
						//업데이트 할 것인데, 파일첨부까지 생각해야 함. 그래서 트랜잭션을 걸어야 하기 때문에 try catch
						try 
						{
							//의수 여기서 잠시멈춤
							if(wdFBoardService.boardUpdate(wdFBoard) > 0) 
							{
								ajaxResponse.setResponse(0, "Success");
							}
							else 
							{
								ajaxResponse.setResponse(500, "Internal Server Error");
							}
						}
						catch(Exception e) 
						{
							logger.error("[WDFBoardController] updateProc Exception", e);
							ajaxResponse.setResponse(500, "Internal Server Error");
						}
						
					}
					else 
					{
						//본인 게시물이 아님
						ajaxResponse.setResponse(404, "Not The Owner");
					}
				}
				else 
				{
					ajaxResponse.setResponse(401, "Not Found");
				}
			}
			else 
			{
				ajaxResponse.setResponse(400, "Bad Request");
			}
			
			
			return ajaxResponse;
		}
		
		
		
		//수정페이지에서 파일 삭제
		@RequestMapping(value="/board/deleteFileProc", method=RequestMethod.POST)
		@ResponseBody
		public Response<Object> deleteFileProc(HttpServletRequest request, HttpServletResponse response)
		{
			Response<Object> ajaxResponse = new Response<Object>();
			String cookieUserId = CookieUtil.getHexValue(request, AUTH_COOKIE_NAME);
			long bSeq = HttpUtil.get(request, "bSeq", (long)0);
			
			if(bSeq>0) 
			{
				WDFBoard wdFBoard = wdFBoardService.wdFBoardView(bSeq);
				
				if(wdFBoard != null) 
				{
					if(StringUtil.equals(cookieUserId, wdFBoard.getUserId())) 
					{
						WDBoardFile wdFBoardFile = wdFBoardService.fBoardFileSelect(bSeq);
						
						wdFBoard.setWdBoardFile(wdFBoardFile);
						
						if(wdFBoardFile != null) 
						{
							//파일 삭제
							try 
							{
								if(wdFBoardService.boardFileDelete(wdFBoard) > 0) 
								{
									ajaxResponse.setResponse(0, "Success");
								}							
							}
							catch(Exception e) 
							{
								logger.debug("[WDFBoardController] deleteFileProc Exception", e);
								ajaxResponse.setResponse(500, "Bad Request");
							}
						}
						else 
						{
							ajaxResponse.setResponse(404, "Bad Request");
						}
					}
					else 
					{
						ajaxResponse.setResponse(401, "Bad Request");
					}
				}
			}
			else 
			{
				ajaxResponse.setResponse(404, "Bad Request");
			}
			
			return ajaxResponse;
		}
		
	   
}
