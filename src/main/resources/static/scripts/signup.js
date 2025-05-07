const loginBtnElem=document.getElementById('login-btn');
const usernameElem=document.getElementById('username');
const passwordElem=document.getElementById('password');
const confirmPasswordElem=document.getElementById('confirm-password');
const emailElem=document.getElementById('email');
const signupBtnElem=document.getElementById('signup-btn');
const signupFormElem=document.getElementById('signup-form');
let codespace=3;
let URL;
if(codespace==1){
    URL='https://friendly-space-sniffle-jjqg44gjp9v525r9x-3000.app.github.dev';
} else if(codespace==2){
    URL='https://fantastic-pancake-7v7p4q766jrg3pg7q-3000.app.github.dev'
}else if(codespace==3){
    //srping-boot project
    URL='https://refactored-winner-pj9x6qjjjgjc7794-8080.app.github.dev';
}

const api=axios.create({
    baseURL:URL
    ,withCredentials:true
})
signupFormElem.addEventListener('submit',(event)=>{
    event.preventDefault();
})

loginBtnElem.addEventListener('click',()=>{
    window.location='/login/login.html';
})

async function signupAttempt(){
    const username=usernameElem.value;
    const password=passwordElem.value;
    const confirmPassword=passwordElem.value;
    const email=emailElem.value;
    if(password!=confirmPassword){
        return response.json({
            status:500,
            message:'Both Passwords do not match'
        })
    }

    const response=await api.post('/api/auth/signup',{
            username,
            email,
            password
        },
        {
           headers:{
             'Content-Type': 'application/json'
           }
    })

    const signupResponse=await response.data;
    if(signupResponse.status==200){
        console.log('singu successfull');
        localStorage.setItem('loginCredentials',JSON.stringify({
            username,
            token:signupResponse.token
        }))
        document.cookie=`token=${signupResponse.token}`
        alert('localstorage also updated');
        window.location='/onlineCompilor.html';
    } else {
        console.log('signup failed : '+JSON.stringify(signupResponse));
    }
}

signupBtnElem.addEventListener('click',async ()=>{
    await signupAttempt(); 
})
