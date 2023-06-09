import { onMount, onCleanup, createSignal, Component } from "solid-js";
import { Chart, CategoryScale, LinearScale, LineController, PointElement, LineElement, Title } from 'chart.js';
import moment from "moment";

// Register the controllers, elements, scales, and plugins we'll be using
Chart.register(LineController, LinearScale, PointElement, LineElement, Title, CategoryScale);

function formatDateString(str: string): string {
  const date = moment(new Date(str));
  switch (str.length) {
    // year: 2023
    case 4: return date.format("yyyy");
    // year-month: 2023-06
    case 7: return date.format("yyyy-MM");
    // year-month-day: 2023-06-23
    case 10: return date.format("yyyy-MM-DD");
    // year-month-day hour:minute 2023-06-23T23:59
    default: return date.format("HH:mm");
  }
}

export const CityChart: Component<{ data: [string, number | null][] }> = (props) => {
  const [canvas, setCanvas] = createSignal<HTMLCanvasElement>();
  let chart: Chart;
  const data: [string, number | null][] = props.data.map(([dateStr, value]) => [
    formatDateString(dateStr),
    value,
  ]);

  // Split the data into two arrays for the x and y axis
  const timestamps = data.map(item => item[0]);
  const temperatures = data.map(item => item[1]);
  
  onMount(() => {
    const ctx = canvas()!.getContext('2d')!;

    chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: timestamps,
        datasets: [{
          label: 'Temperature',
          data: temperatures,
          fill: false,
          borderColor: 'rgb(75, 192, 192)',
          tension: 0.1,
        }]
      },
      options: {
        spanGaps: false,
        plugins: {
            title: {
                // display: true,
                text: "Rīga",
            },
        },
        scales: {
            x: {
                beginAtZero: true,
                title: {
                    // display: true,
                    text: 'Day'
                },
            },
            y: {
                beginAtZero: true,
                title: {
                    // display: true,
                    text: 'Temperature (°C)'
                }
            }
        }
      }
    });
  });

  onCleanup(() => {
    chart.destroy();
  });

  return (
    <canvas ref={setCanvas} />
  );
}