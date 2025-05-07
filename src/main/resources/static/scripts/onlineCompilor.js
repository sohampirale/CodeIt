const runCodeBtnElem = document.getElementById('js-run-code-btn');
const codeAreaElem = document.getElementById('js-code-area');
const selectedLangElem = document.getElementById('language-select');
const outputAreaElem = document.getElementById('js-output-area');
const logoutBtnElem=document.getElementById('logout-btn');
const loginCredentials=JSON.parse(localStorage.getItem('loginCredentials'));
const greetUserElem=document.querySelector('.greet-user');
const historyListElem=document.querySelector('.history-list');
let userData;
let temoCodeStore=``;
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
    baseURL:URL,
    credentials:'include'
})
const toggleSidebarBtn = document.getElementById('toggle-sidebar-btn');
const sidebar = document.querySelector('.sidebar');

toggleSidebarBtn.addEventListener('click', () => {
  sidebar.classList.toggle('active');
});

const newCodeElem=document.createElement('div');
newCodeElem.classList.add('history-item');
newCodeElem.innerHTML=`New Code`;
newCodeElem.addEventListener('click',()=>{
    reloadPreviousCode();
    outputAreaElem.value=``;
})
historyListElem.appendChild(newCodeElem);

function reloadPreviousCode(){
    codeAreaElem.value=temoCodeStore
    temoCodeStore=''
}

async function getDataFromBackend() {
    const response = await api.get('/api/code/history', {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${loginCredentials.token}` // Assuming loginCredentials has a token field
        },
        withCredentials: true
    });

    const getDataResponse = await response.data;
    if (getDataResponse.status != 200) {
        historyListElem.innerHTML = `<div class="history-item">Let's make the first compilation!:)</div>`;
    } else {
        const history = getDataResponse.history;
        history.forEach((work) => {
            const workElem = document.createElement('div');
            workElem.classList.add('H' + work.id);
            workElem.classList.add('history-item');
            workElem.innerHTML = work.code;
            workElem.addEventListener('click', () => {
                if (temoCodeStore == '') temoCodeStore = codeAreaElem.value;
                codeAreaElem.value = work.code;
                outputAreaElem.value = work.output;
            });
            historyListElem.appendChild(workElem);
        });
    }
    console.log('Data received:', JSON.stringify(getDataResponse));
}


if(!loginCredentials){
    alert('You need to login first to access this page');
    window.location.href=URL;
} else {
    console.log('Welcome : '+loginCredentials.username);
    greetUserElem.innerHTML=`Welcome ${loginCredentials.username}`
    getDataFromBackend();
}

codeAreaElem.addEventListener('keydown',async (event)=>{
    if(event.ctrlKey&& event.key=='Enter'){
        await work();
    }
})

logoutBtnElem.addEventListener('click', async () => {
    localStorage.removeItem('loginCredentials');
    const response = await api.post('/api/auth/logout', loginCredentials, {
        headers: {
            "Content-Type": "application/json"
        }
    });

    const logoutResponse = await response.data;
    console.log(JSON.stringify(logoutResponse));
    if (logoutResponse.status == 200) {
        console.log('Login credentials removed from localStorage');
        console.log(logoutResponse.message);
        window.location.href = URL;  // Redirect to home page after logout
    } else {
        console.log('Logout API call failed');
        alert('Logout failed, please try again!');
    }
});

async function work(){
    
    if(codeAreaElem.value.length==0){
        alert('Code cannot be empty')
        return;
    }
    console.log(`POST request sent to ${URL}/run`);
    try {
        alert('Sending POST req to /api/code/run');
        console.log('trying now from frontend');
    
        const response = await api.post(`/api/code/run`, 
            {
                code: codeAreaElem.value,
                language: selectedLangElem.value,
            }, 
            {
                headers: {
                    'Content-Type': 'application/json'
                },
                withCredentials: true  // IMPORTANT if you're sending cookies
            }
        );
    
        const finalOutput = response.data;
        console.log('Frontend finalOutput:', finalOutput);
    
        // Display status first
        outputAreaElem.value = `Status: ${finalOutput.status}`;
    
        if (finalOutput.stdout && finalOutput.stdout.trim() !== "") {
            alert("Code execution successful ✅");
            outputAreaElem.value += `\nOutput:\n${finalOutput.stdout}`;
        } else if (finalOutput.stderr && finalOutput.stderr.trim() !== "") {
            alert("Code execution had errors ❌");
            outputAreaElem.value += `\nError:\n${finalOutput.stderr}`;
        } else {
            alert("Empty response received ❗");
            outputAreaElem.value += `\nNo output or error received.`;
        }
    
    } catch (err) {
        console.error("Execution failed:", err);
    
        if (err.response) {
            const status = err.response.status;
            const finalOutput = err.response.data;
            
            outputAreaElem.value = `Backend Error (${status}):\n${finalOutput}`;
            alert("Backend error occurred 🚫");
        } else {
            outputAreaElem.value = 'Could not connect to server or unknown error occurred.';
            alert("Network error or unexpected issue");
        }
    }
}

runCodeBtnElem.addEventListener('click', work);