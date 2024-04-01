import { useRef, useEffect } from 'react';
import Chart from 'chart.js/auto';

type CharAreaProps = {
    data: number[][];
};

export function ChartArea({ data }: CharAreaProps) {
    const prevSum = data[0].reduce((prev, cur) => prev + cur, 0); // 이전주 지출 총액
    const presSum = data[1].reduce((prev, cur) => prev + cur, 0); // 이번주 지출 총액

    let savingState; // 지출 상태 (절감 / 유지 / 증가)
    if (prevSum > presSum) {
        savingState = '절감!';
    } else if (prevSum === presSum) {
        savingState = '유지 중!';
    } else {
        savingState = '증가!';
    }

    const savingRate = ((presSum / prevSum) * 100).toFixed(1); // 절감률
    const spendDiff = Math.abs(presSum - prevSum).toLocaleString(); // 지출 차액

    return (
        <>
            <LineChart data={data} />
            <div className="chartArea__bottom">
                <div className="chartArea__bottom-left">
                    <div className="chartArea__bottom-left-prev">
                        <div className="chartArea__bottom-left-prev-title">지난 주 지출액</div>
                        <div className="chartArea__bottom-left-prev-value">
                            <span className="value">{prevSum.toLocaleString()}</span>
                            <span className="unit">원</span>
                        </div>
                    </div>
                    <div className="chartArea__bottom-left-pres">
                        <div className="chartArea__bottom-left-pres-title">이번 주 지출액</div>
                        <div className="chartArea__bottom-left-pres-value">
                            <span className="value">{presSum.toLocaleString()}</span>
                            <span className="unit">원</span>
                        </div>
                    </div>
                </div>
                <div className="chartArea__bottom-right">
                    <div className="chartArea__bottom-right-prev">
                        <div className="chartArea__bottom-right-prev-title">전 주 대비</div>
                        <div className="chartArea__bottom-right-prev-value">
                            {savingRate !== '100.0' ? `${savingRate}%` : ''} {savingState}
                        </div>
                    </div>
                    <div className="chartArea__bottom-right-pres">
                        <div className="chartArea__bottom-right-pres-title">전 주 대비</div>
                        <div className="chartArea__bottom-right-pres-value">
                            {savingRate !== '100.0' ? `${spendDiff}원` : ''} {savingState}
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

type LineCharProps = {
    data: number[][];
};

const LineChart = ({ data }: LineCharProps) => {
    const chartContainer = useRef<HTMLCanvasElement | null>(null);

    useEffect(() => {
        if (!chartContainer.current) return;

        const ctx = chartContainer.current.getContext('2d')!;

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'],
                datasets: [
                    {
                        label: '지난주',
                        data: data[0],
                        fill: false,
                        borderColor: 'rgb(136, 186, 83)',
                        pointRadius: 5,
                        pointBackgroundColor: 'rgb(136, 186, 83)',
                        backgroundColor: 'rgb(136, 186, 83)',
                        pointStyle: 'circle',
                        tension: 0.1,
                    },
                    {
                        label: '이번주',
                        data: data[1],
                        fill: false,
                        borderColor: 'rgb(235, 192, 105)',
                        pointRadius: 5,
                        pointBackgroundColor: 'rgb(235, 192, 105)',
                        backgroundColor: 'rgb(235, 192, 105)',
                        pointStyle: 'circle',
                        tension: 0.1,
                    },
                ],
            },
            options: {
                plugins: {
                    legend: {
                        align: 'end',
                        position: 'top',
                        onClick: () => {},
                        labels: {
                            usePointStyle: true,
                            pointStyle: 'circle',
                        },
                    },
                },
                scales: {
                    x: {
                        grid: {
                            display: false,
                        },
                        offset: true,
                    },
                    y: {
                        ticks: {
                            callback: function (value) {
                                return '';
                            },
                        },
                        grid: {
                            display: false,
                        },
                        offset: true,
                    },
                },
            },
        });
    }, []);

    return (
        <div className="teamInfoTeamExpenditure__chartArea">
            <canvas className="teamInfoTeamExpenditure__chartArea-chart" ref={chartContainer} height="100"></canvas>
        </div>
    );
};
