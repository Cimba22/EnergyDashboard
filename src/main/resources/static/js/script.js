const apiUrl = '/energy';

function fetchAllData() {
    fetch(`${apiUrl}/`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            displayData(data);
            visualizeData(data); // Визуализация данных
        })
        .catch(error => console.error('Error fetching all data:', error));
}

function fetchFilteredData() {
    const minEnergy = document.getElementById('minEnergyFilter').value;
    const maxEnergy = document.getElementById('maxEnergyFilter').value;
    const startDate = document.getElementById('startDateFilter').value;
    const endDate = document.getElementById('endDateFilter').value;
    const stationName = document.getElementById('stationNameFilter').value;

    const params = new URLSearchParams();
    if (minEnergy) params.append('minEnergyConsumed', minEnergy);
    if (maxEnergy) params.append('maxEnergyConsumed', maxEnergy);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (stationName) params.append('stationName', stationName);

    fetch(`${apiUrl}/filter?${params.toString()}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            displayData(data);
            visualizeData(data); // Визуализация данных
        })
        .catch(error => console.error('Error fetching filtered data:', error));
}


function displayData(data) {
    const tableBody = document.getElementById('dataTable').getElementsByTagName('tbody')[0];
    tableBody.innerHTML = ''; // Очистка таблицы перед вставкой новых данных

    if (data.length === 0) {
        const row = tableBody.insertRow();
        const cell = row.insertCell(0);
        cell.colSpan = 4;
        cell.textContent = 'No data available';
    } else {
        data.forEach(record => {
            const row = tableBody.insertRow();
            row.insertCell(0).textContent = record.stationName;
            row.insertCell(1).textContent = record.date;
            row.insertCell(2).textContent = record.energyProduced;
            row.insertCell(3).textContent = record.energyConsumed;
        });
    }
}

function downloadData(format) {
    const url = `/energy/download/${format}`;
    window.location.href = url;
}

function uploadFile() {
    const fileInput = document.getElementById('jsonFile');
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    fetch(apiUrl + '/upload', {
        method: 'POST',
        body: formData
    })
        .then(response => response.text())
        .then(data => {
            document.getElementById('uploadStatus').textContent = data;
            fetchAllData(); // Обновить таблицу после загрузки
        })
        .catch(error => console.error('Error uploading file:', error));
}

function compareData() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    fetch(`${apiUrl}/compare?startDate=${startDate}&endDate=${endDate}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            console.log('Comparison data:', data); // Выводим данные в консоль для отладки
            displayComparisonData(data);
            visualizeData(data); // Визуализация данных для сравнения
        })
        .catch(error => console.error('Error comparing data:', error));
}

function displayComparisonData(data) {
    const tableBody = document.getElementById('comparisonTable').getElementsByTagName('tbody')[0];
    tableBody.innerHTML = ''; // Очистка таблицы перед вставкой новых данных

    data.forEach(record => {
        const row = tableBody.insertRow();
        row.insertCell(0).textContent = record.stationName;
        row.insertCell(1).textContent = record.date;
        row.insertCell(2).textContent = record.energyProduced;
        row.insertCell(3).textContent = record.energyConsumed;
    });
}

function visualizeData(data) {
    const labels = data.map(record => record.date); // Массив дат
    const energyProducedData = data.map(record => record.energyProduced); // Массив произведенной энергии
    const energyConsumedData = data.map(record => record.energyConsumed); // Массив потребленной энергии

    const ctx = document.getElementById('energyChart').getContext('2d');
    const energyChart = new Chart(ctx, {
        type: 'line', // Тип графика
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Energy Produced',
                    data: energyProducedData,
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: 'Energy Consumed',
                    data: energyConsumedData,
                    borderColor: 'rgba(255, 99, 132, 1)',
                    borderWidth: 1,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}
