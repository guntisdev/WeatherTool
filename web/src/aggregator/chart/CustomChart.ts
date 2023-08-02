import {
    BarElement,
    BarController,
    CategoryScale,
    Chart,
    LinearScale,
    LineController,
    PointElement,
    LineElement,
    Title,
} from "chart.js";

// Register the controllers, elements, scales, and plugins we'll be using
Chart.register(
    BarElement,
    BarController,
    LineController,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    CategoryScale,
);

// Chart.defaults.backgroundColor = "#FF0000";

const barChartData = ["precipitation", "sunDuration"];

export function createCustomChart(
    ctx: CanvasRenderingContext2D,
    showBig: boolean,
    timestamps: string[],
    values: Array<number | null>,
    city: string,
    field: string,
    granularity: string,
): Chart {
    const dataConfig = {
        labels: timestamps,
        datasets: [{
            label: field,
            data: values,
            fill: false,
            borderColor: "rgb(75, 192, 192)",
            backgroundColor: "rgb(75, 192, 192)",
            tension: 0.1,
        }]
    };

    const optionsConfig = {
        plugins: {
            title: {
                display: showBig,
                text: city,
            },
        },
        scales: {
            x: {
                beginAtZero: true,
                title: {
                    display: showBig,
                    text: granularity,
                },
            },
            y: {
                beginAtZero: true,
                title: {
                    display: showBig,
                    text: field,
                }
            }
        },
    };

    if (!barChartData.includes(field)) {
        dataConfig.datasets[0].backgroundColor = "#FFFFFF";
    }

    return new Chart(ctx, {
        type: barChartData.includes(field) ? "bar" : "line",
        data: dataConfig,
        options: {
            ...optionsConfig,
            animation: false,
            spanGaps: false,
        },
        plugins: [ canvas_bg_plugin ],
      });
}

const canvas_bg_plugin = {
    id: "canvas_bg_plugin",
    beforeDraw: (chart: any, args: any, options: any) => {
      const { ctx } = chart;
      ctx.save();
      ctx.fillStyle = "#FFFFFF";
      ctx.fillRect(0, 0, ctx.canvas.width, ctx.canvas.height);
      ctx.restore();
    },
};