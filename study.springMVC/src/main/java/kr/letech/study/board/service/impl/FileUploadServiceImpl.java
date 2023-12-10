package kr.letech.study.board.service.impl;

import kr.letech.study.board.dao.PostDAO;
import kr.letech.study.board.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;

import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl {
    @Value("${file.directory}")
    private String uploadFolder;
    
    private final PostDAO postDao;

    public List<FileVO> uploadFile(List<MultipartFile> files) {
        List<FileVO> fileList = new ArrayList<>();
        File uploadPath = new File(uploadFolder, getFolder());
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        for (MultipartFile file : files) {
            String orgNm = file.getOriginalFilename();
            String extension = getExtension(orgNm);
            String uuid = UUID.randomUUID().toString();
            String saveNm = uuid + extension;

            FileVO fileVO = new FileVO();
            File saveFile = new File(uploadPath, saveNm);

            try {
                file.transferTo(saveFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fileVO.setFileId(uuid);
            fileVO.setFileOrgNm(orgNm);
            fileVO.setFileSaveNm(saveNm);
            fileVO.setFileSize(file.getSize());
            fileVO.setFilePath(uploadFolder + "/" + getFolder());
            fileList.add(fileVO);
        }

        return fileList;
    }

    public String getFolder() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String str = sdf.format(date);
        return str;
    }

    public String getExtension(String fileOrgNm) {
        String extension = fileOrgNm.substring(fileOrgNm.lastIndexOf("."));
        return extension;
    }
    
    public void fileDownload(String fileId, HttpServletResponse response){
    	FileVO fileVO = postDao.getFileById(fileId);
    	String filename = fileVO.getFileOrgNm();
    	File file = new File(fileVO.getFilePath(), fileVO.getFileSaveNm());
        String encodedFileName;

		try {
			encodedFileName = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
			response.setContentType("application/download");
	    	response.setContentLength((int) file.length());
	    	response.setHeader("Content-Disposition", "attatchment;filename=\"" + encodedFileName + "\"");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	try (OutputStream out = response.getOutputStream();
			FileInputStream fis = new FileInputStream(file);) {
			FileCopyUtils.copy(fis, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void preview(FileVO fileVO, HttpServletResponse response) {
        File file = new File(fileVO.getFilePath(), fileVO.getFileSaveNm());
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType != null && contentType.startsWith("image")) { //이미지 일 때만
                response.setContentType(contentType);

                Thumbnails.of(file)
                        .size(100, 100)
                        .outputQuality(0.8)
                        .toOutputStream(response.getOutputStream());
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
