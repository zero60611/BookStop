package com.example.demo.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.RoleRepository;
import com.example.demo.dao.UserRepository;
import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.payload.request.LoginRequest;
import com.example.demo.payload.request.SignupRequest;
import com.example.demo.payload.response.JwtResponse;
import com.example.demo.payload.response.MessageResponse;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.security.service.UserDetailsImpl;



@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private static final String UPLOAD_PATH = "C:\\_spring\\worksapce\\SpringBoot-VUE3\\src\\main\\resources\\static\\";

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;
	
	
	//	– Requests:
	//
	//		LoginRequest: { username, password }
	//		SignupRequest: { username, email, password }
	//– Responses:
	//
	//		JwtResponse: { token, type, id, username, email, roles }
	//		MessageResponse: { message }
	//登入
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		//loginRequest 登入請求的條件設定getUsername等的條件符合
		//認證
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		//透過認證後的物件更新安全環境
		SecurityContextHolder.getContext().setAuthentication(authentication);
		//建立一個jwt 透過認證後的物件
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		//從認證後的資訊尋找使用者明細
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(new JwtResponse(jwt, 
												 userDetails.getId(), 
												 userDetails.getUsername(), 
												 userDetails.getEmail(), 
												 roles));
	}

	//註冊  signUpRequest去判斷註冊的條件
	//check existing username/email
	@PostMapping("/signup")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		//建立使用者
		User user = new User(signUpRequest.getUsername(), 
							 signUpRequest.getEmail(),
							 encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "mod":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		//存到資料庫
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
	
	
	/**
	 * 上傳圖片
	 */
	@PostMapping("/upload")
	public String singleFileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		try {
			byte[] bytes = file.getBytes();
			String imageFileName = file.getOriginalFilename();
			System.out.println("imageFileName-->"+imageFileName);
			String fileName = UpPhotoNameUtils.getPhotoName("img", imageFileName);
			System.out.println("fileName-->"+fileName);
			Path path = Paths.get(UPLOAD_PATH + fileName);
			Files.write(path, bytes);
			System.out.println(fileName + "\n");
			return fileName;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * 處理圖片名字的工具類別
	 */
	public static class UpPhotoNameUtils {
		public static String getPhotoName(String name, String imageFileName) {
			String fileName = name;//name = img
			System.out.println("imageFileName=="+imageFileName);
			System.out.println("UpPhotoNameUtils=fileName"+fileName);
			Calendar cal = Calendar.getInstance();
			// 如果年的目录不存在，创建年的目录
			int year = cal.get(Calendar.YEAR);
			fileName = fileName + "_" + year;
			// 如果月份不存在，创建月份的目录
			int month = cal.get(Calendar.MONTH) + 1;
			fileName = fileName + "_";
			if (month < 10) {
				fileName = fileName + "0";
			}
			fileName = fileName + month;
			// 生成文件名的日部分
			int day = cal.get(Calendar.DAY_OF_MONTH);
			fileName = fileName + "_";
			if (day < 10) {
				fileName = fileName + "0";
			}
			fileName = fileName + day;
			// 生成文件名的小时部分
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if (hour < 10) {
				fileName = fileName + "0";
			}
			fileName = fileName + hour;
			// 生成文件名的分钟部分
			int minute = cal.get(Calendar.MINUTE);
			if (minute < 10) {
				fileName = fileName + "0";
			}
			fileName = fileName + minute;
			// 生成文件名的秒部分
			int second = cal.get(Calendar.SECOND);
			if (second < 10) {
				fileName = fileName + "0";
			}
			fileName = fileName + second;
			// 生成文件名的毫秒部分
			int millisecond = cal.get(Calendar.MILLISECOND);
			if (millisecond < 10) {
				fileName = fileName + "0";
			}
			if (millisecond < 100) {
				fileName = fileName + "0";
			}
			fileName = fileName + millisecond;
			System.out.println("fileNamefileName"+fileName);
			// 生成文件的扩展名部分,只截取最后单位 =lastIndexOf(int ch): 返回指定字符在此字符串中最后一次出现处的索引
			String endName = imageFileName.substring(imageFileName.lastIndexOf("."));// 截取之后的值
			fileName = fileName + endName;
			return fileName;
		}
	}
	
	 //使用流將圖片輸出
		@GetMapping("/getImage/{name}")
		
	    public void getImage(HttpServletResponse response, @PathVariable("name") String name) throws IOException {
	        System.out.println("addaddadd");
			response.setContentType("image/jpeg;charset=utf-8");
	        response.setHeader("Content-Disposition", "inline; filename=girls.png");
	        ServletOutputStream outputStream = response.getOutputStream();
//	        System.out.println("static"+name);
	        outputStream.write(Files.readAllBytes(Paths.get(UPLOAD_PATH).resolve(name)));
	        outputStream.flush();
	        outputStream.close();
	    }
}