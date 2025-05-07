let codespace=3;
let URL;
if(codespace==1){
    URL='https://friendly-space-sniffle-jjqg44gjp9v525r9x-3000.app.github.dev';
} else if(codespace==2){
    URL='https://fantastic-pancake-7v7p4q766jrg3pg7q-3000.app.github.dev'
} else if(codespace==3){
    //srping-boot project
    URL='https://refactored-winner-pj9x6qjjjgjc7794-8080.app.github.dev';
}

const api=axios.create({
    baseURL:URL,
    withCredentials:true
})

// async function defaultLogin(){
//     const response=await api.get(URL+'/app')   
// }

// defaultLogin();

const loginCredentials=JSON.parse(localStorage.getItem('loginCredentials'));
const signupBtnElem=document.querySelector('.signup-btn');

signupBtnElem.addEventListener('click',()=>{
    console.log('signup btn clicked');
    window.location.href='/signup/signup.html';
})



// if(loginCredentials){
//     console.log('user is already logged in ');
//     console.log('user data : '+JSON.stringify(loginCredentials));
//     window.location=URL+'/app';
// } else {
//     console.log('user not currently logged in');
// }

const usernameElem=document.getElementById('username');
const passwordElem=document.getElementById('password');
const loginBtnElem=document.getElementById('login-btn');

async function loginAttempt(){
    if(!usernameElem.value||!passwordElem.value){
        alert('fields cannot be empty')
        return;
    }
    alert("sending post to /api/auth/login");
    const response = await api.post(
        '/api/auth/login',
        {
            username: usernameElem.value,
            password: passwordElem.value
        },
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );
    
    const loginResponse=await response.data;
    console.log('login response : '+loginResponse);
    
    if(loginResponse.status!=200){
        console.log('Failed to Login');
        console.log(loginResponse.message);
    } else {
        console.log('login successful!');
        console.log('token received at frontend is : '+loginResponse.token);        
        localStorage.setItem('loginCredentials',JSON.stringify({
            username:usernameElem.value,
            token:loginResponse.token
        }));
        const date=new Date();
        const fiveminsLater=new Date(date.getTime()+1*60*1000)
        console.log('token stored to the localstorage');
        alert('cookie also set')
        // alert('going to onlineCompilor.html')
        window.location.href='onlineCompilor.html'
    }
}

loginBtnElem.addEventListener('click',async (event)=>{
    event.preventDefault();
    await loginAttempt();
})